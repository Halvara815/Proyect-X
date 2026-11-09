package com.proyectx.focus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Optional DNS-only VPN. It only accepts unfragmented UDP DNS packets addressed to the local
 * resolver, never records domains, and does not route general web traffic through a remote VPN.
 */
public final class DnsVpnService extends VpnService {
    public static final String ACTION_START = "com.proyectx.focus.action.START_DNS_VPN";
    public static final String ACTION_STOP = "com.proyectx.focus.action.STOP_DNS_VPN";
    public static final String STATE_STOPPED = "STOPPED";
    public static final String STATE_STARTING = "STARTING";
    public static final String STATE_ACTIVE = "ACTIVE";
    public static final String STATE_ERROR = "ERROR";
    public static final String STATE_REVOKED = "REVOKED";
    private static final String PREFS = "proyect_x_dns_vpn";
    private static final String STATE = "state";
    private static final String DETAIL = "detail";
    private static final String UPDATED_AT = "updated_at";
    private static final String CHANNEL_ID = "proyect_x_dns_protection";
    private static final int NOTIFICATION_ID = 4_201;
    private static final int WORKERS = 2;
    private static final int VPN_PACKET_BUFFER_BYTES = DnsVpnPacketCodec.MAX_PACKET_BYTES;
    private static final long DNS_TIMEOUT_MILLIS = 3_000L;
    private static final String UPSTREAM_ADDRESS = "1.1.1.1";
    private static final int UPSTREAM_PORT = 53;
    private static volatile boolean running;
    private static volatile DnsVpnService activeInstance;
    private final AtomicBoolean stopping = new AtomicBoolean();
    private final Object tunnelWriteLock = new Object();
    private volatile ParcelFileDescriptor tunnel;
    private volatile ExecutorService workers;
    private volatile FileOutputStream tunnelOutput;
    private volatile List<Rule> bundledRules = Collections.emptyList();
    private RuleStore ruleStore;

    public static final class Status {
        private final String state;
        private final String detail;
        private final long updatedAtMillis;

        private Status(String state, String detail, long updatedAtMillis) {
            this.state = state;
            this.detail = detail;
            this.updatedAtMillis = updatedAtMillis;
        }

        public String state() {
            return state;
        }

        public String detail() {
            return detail;
        }

        public long updatedAtMillis() {
            return updatedAtMillis;
        }
    }

    public static Status readStatus(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, MODE_PRIVATE);
        String state = preferences.getString(STATE, STATE_STOPPED);
        String detail = preferences.getString(DETAIL, "Filtro DNS detenido.");
        long updatedAt = preferences.getLong(UPDATED_AT, 0L);
        if (!running && (STATE_ACTIVE.equals(state) || STATE_STARTING.equals(state))) {
            return new Status(
                    STATE_STOPPED,
                    "Filtro DNS detenido tras reinicio del proceso; puedes reactivarlo cuando quieras.",
                    updatedAt);
        }
        return new Status(state, detail, updatedAt);
    }

    /**
     * Recovers an active DNS tunnel after an app process restart if the user left the filter active
     * and Android still holds VPN consent for this package. If consent was cleared (e.g. device reboot
     * without system always-on VPN), reconciles persisted state to STOPPED.
     */
    public static boolean recoverIfNeeded(Context context) {
        if (running) return false;
        SharedPreferences preferences = context.getSharedPreferences(PREFS, MODE_PRIVATE);
        String state = preferences.getString(STATE, STATE_STOPPED);
        if (!STATE_ACTIVE.equals(state)) return false;
        boolean notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
        if (!notificationsGranted || VpnService.prepare(context) != null) {
            writeStatus(
                    context, STATE_STOPPED, "Filtro DNS detenido tras reinicio; puedes reactivarlo cuando quieras.");
            return false;
        }
        try {
            writeStatus(context, STATE_STARTING, "Recuperando el filtro DNS local tras reinicio del proceso.");
            running = true;
            Intent intent = new Intent(context, DnsVpnService.class).setAction(ACTION_START);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent);
            else context.startService(intent);
            return true;
        } catch (RuntimeException exception) {
            running = false;
            writeStatus(context, STATE_ERROR, "No se pudo recuperar el filtro DNS automáticamente; pulsa reintentar.");
            return false;
        }
    }

    /**
     * Immediate in-app kill switch. Synchronously tears down any active tunnel instance, stops the
     * service, removes the foreground notification, and persists STOPPED.
     */
    public static void killSwitch(Context context) {
        DnsVpnService current = activeInstance;
        if (current != null) {
            current.stopFilter(STATE_STOPPED, "Filtro DNS desactivado por el kill switch de la app.");
        }
        running = false;
        writeStatus(context, STATE_STOPPED, "Filtro DNS desactivado por el kill switch de la app.");
        try {
            context.stopService(new Intent(context, DnsVpnService.class));
        } catch (RuntimeException ignored) {
            // Service was already stopped.
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) manager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        activeInstance = this;
        ruleStore = new RuleStore(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            stopFilter(STATE_STOPPED, "Filtro DNS detenido por la persona usuaria.");
            return START_NOT_STICKY;
        }
        boolean recoverActiveFilter = intent == null
                && STATE_ACTIVE.equals(getSharedPreferences(PREFS, MODE_PRIVATE).getString(STATE, STATE_STOPPED));
        if (ACTION_START.equals(action) || VpnService.SERVICE_INTERFACE.equals(action) || recoverActiveFilter) {
            startFilter();
            return START_STICKY;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onRevoke() {
        stopFilter(STATE_REVOKED, "Android revocó la VPN; el filtro DNS ya no está activo.");
        super.onRevoke();
    }

    @Override
    public void onDestroy() {
        // A system process restart may recreate this sticky service with a null intent. Keep an active
        // state for that narrow case; explicit stop, revoke, and failures all set stopping first.
        if (!stopping.get()) releaseResources();
        super.onDestroy();
    }

    private synchronized void startFilter() {
        if (tunnel != null || stopping.get()) return;
        if (VpnService.prepare(this) != null) {
            running = false;
            writeStatus(STATE_ERROR, "Falta la aprobación de VPN de Android.");
            stopSelf();
            return;
        }
        try {
            bundledRules = BundledDomainPolicy.load(this);
        } catch (IOException | IllegalStateException exception) {
            running = false;
            writeStatus(STATE_ERROR, "No se pudo verificar la política DNS incorporada.");
            stopSelf();
            return;
        }
        writeStatus(STATE_STARTING, "Preparando el resolvedor DNS local.");
        running = true;
        startForeground(NOTIFICATION_ID, notification("Iniciando filtro DNS"));
        try {
            ParcelFileDescriptor established = new Builder()
                    .setSession("Proyect X DNS")
                    .setMtu(1_500)
                    .addAddress("10.239.0.2", 32)
                    .addRoute("10.239.0.1", 32)
                    .addDnsServer("10.239.0.1")
                    .addAddress("fd12:3456:789a::2", 128)
                    .addRoute("fd12:3456:789a::1", 128)
                    .addDnsServer("fd12:3456:789a::1")
                    .establish();
            if (established == null) throw new IOException("Android no creó la interfaz VPN.");
            tunnel = established;
            tunnelOutput = new FileOutputStream(established.getFileDescriptor());
            workers = Executors.newFixedThreadPool(WORKERS);
            writeStatus(
                    STATE_ACTIVE,
                    "Filtro DNS activo: reglas locales + " + BundledDomainPolicy.VERSION + "; sin historial.");
            // The first foreground notification says that the tunnel is starting. Replace it only
            // after establish() succeeds so the visible state agrees with the persisted state.
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.notify(NOTIFICATION_ID, notification("Filtro DNS activo"));
            startTunnelLoop(established);
        } catch (IOException | RuntimeException exception) {
            stopFilter(STATE_ERROR, "No se pudo activar el filtro DNS. Revisa tu red e inténtalo de nuevo.");
        }
    }

    private void startTunnelLoop(ParcelFileDescriptor established) {
        Thread thread = new Thread(() -> readPackets(established), "proyect-x-dns-vpn");
        thread.setDaemon(true);
        thread.start();
    }

    private void readPackets(ParcelFileDescriptor established) {
        byte[] buffer = new byte[VPN_PACKET_BUFFER_BYTES];
        try (FileInputStream input = new FileInputStream(established.getFileDescriptor())) {
            while (!stopping.get()) {
                int count = input.read(buffer);
                if (count <= 0) continue;
                DnsVpnPacket packet = DnsVpnPacketCodec.parseDnsQuery(buffer, count);
                if (packet == null) continue;
                byte[] immutablePacket = packet.dnsPayload();
                ExecutorService executor = workers;
                if (executor != null && !executor.isShutdown()) {
                    executor.execute(() -> resolveAndReply(packet, immutablePacket));
                }
            }
        } catch (IOException exception) {
            if (!stopping.get()) {
                stopFilter(STATE_ERROR, "La interfaz DNS se cerró inesperadamente.");
            }
        }
    }

    private void resolveAndReply(DnsVpnPacket packet, byte[] dnsQuery) {
        if (stopping.get()) return;
        try {
            Calendar now = Calendar.getInstance();
            List<Rule> activeRules = new ArrayList<>(bundledRules);
            activeRules.addAll(ruleStore.list());
            LocalDnsPolicy.Result decision = LocalDnsPolicy.evaluate(
                    dnsQuery,
                    activeRules,
                    ScheduleWindow.dayIndexFromCalendarDayOfWeek(now.get(Calendar.DAY_OF_WEEK)),
                    now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE));
            byte[] dnsResponse = DnsResolutionExecutor.execute(
                    decision, dnsQuery, DNS_TIMEOUT_MILLIS, stopping::get, this::resolveUpstream);
            if (stopping.get()) return;
            byte[] response = DnsVpnPacketCodec.responseFor(packet, dnsResponse);
            synchronized (tunnelWriteLock) {
                if (tunnelOutput != null && !stopping.get()) {
                    tunnelOutput.write(response);
                    tunnelOutput.flush();
                }
            }
        } catch (IllegalArgumentException | IllegalStateException | IOException ignored) {
            // A failed query is allowed to time out. Do not log or persist domains or packet data.
        }
    }

    private byte[] resolveUpstream(byte[] query, long timeoutMillis, DnsResolutionExecutor.Cancellation cancellation)
            throws IOException {
        if (cancellation.isCancelled()) throw new IOException("DNS VPN is stopping.");
        InetAddress upstream = InetAddress.getByName(UPSTREAM_ADDRESS);
        try (DatagramSocket socket = new DatagramSocket()) {
            if (!protect(socket)) throw new IOException("Android refused to protect the DNS socket.");
            socket.connect(upstream, UPSTREAM_PORT);
            socket.setSoTimeout((int) timeoutMillis);
            socket.send(new DatagramPacket(query, query.length));
            byte[] response = new byte[4_096];
            DatagramPacket received = new DatagramPacket(response, response.length);
            try {
                socket.receive(received);
            } catch (SocketTimeoutException exception) {
                throw new IOException("Upstream DNS timeout.", exception);
            }
            if (cancellation.isCancelled()) throw new IOException("DNS VPN is stopping.");
            byte[] copy = new byte[received.getLength()];
            System.arraycopy(received.getData(), received.getOffset(), copy, 0, received.getLength());
            return copy;
        }
    }

    private void stopFilter(String state, String detail) {
        if (!stopping.compareAndSet(false, true)) return;
        writeStatus(state, detail);
        releaseResources();
        stopForeground(true);
        stopSelf();
    }

    private void releaseResources() {
        running = false;
        if (activeInstance == this) activeInstance = null;
        ExecutorService executor = workers;
        if (executor != null) executor.shutdownNow();
        closeTunnel();
    }

    private void closeTunnel() {
        ParcelFileDescriptor descriptor = tunnel;
        tunnel = null;
        tunnelOutput = null;
        if (descriptor != null) {
            try {
                descriptor.close();
            } catch (IOException ignored) {
                // Nothing remains to recover after Android has closed a TUN descriptor.
            }
        }
    }

    private Notification notification(String title) {
        Intent stopIntent = new Intent(this, DnsVpnService.class).setAction(ACTION_STOP);
        PendingIntent stop = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent dashboardIntent = new Intent(this, MainActivity.class);
        PendingIntent dashboard = PendingIntent.getActivity(
                this, 0, dashboardIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return builder.setSmallIcon(com.proyectx.focus.R.drawable.ic_proyect_x)
                .setContentTitle(title)
                .setContentText("Proyect X filtra solo DNS UDP; no guarda consultas.")
                .setContentIntent(dashboard)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(new Notification.Action.Builder(0, "DETENER", stop).build())
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID, "Protección DNS", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Estado visible del filtro DNS local de Proyect X.");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    private void writeStatus(String state, String detail) {
        writeStatus(this, state, detail);
    }

    @SuppressLint("ApplySharedPref") // Status must survive before a foreground service stops or is revoked.
    private static void writeStatus(Context context, String state, String detail) {
        context.getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(STATE, state)
                .putString(DETAIL, detail)
                .putLong(UPDATED_AT, System.currentTimeMillis())
                .commit();
    }
}
