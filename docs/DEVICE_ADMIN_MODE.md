# Ruta administrada: Device Owner y Profile Owner

Proyect X actual es una app de consumo local. No solicita ni posee rol Device Owner, Profile Owner, administrador de dispositivo ni permisos de administración remota.

En un dispositivo gestionado, un administrador con autoridad separada podría configurar una VPN always-on mediante `DevicePolicyManager`. Esa ruta no está disponible para una instalación de consumo normal y no debe anunciarse como característica de Proyect X.

Antes de considerar una integración administrada se exige:

1. Un producto y paquete de administración separados, con titularidad y soporte empresarial definidos.
2. Consentimiento/contrato de organización, política de privacidad y revisión legal propios.
3. `VpnService` funcional, pruebas de recuperación y aviso claro de que lockdown puede cortar conectividad si la VPN falla.
4. Matriz de fabricantes, Android/API, perfiles de trabajo y procedimiento de desinstalación/reversión.

La documentación oficial confirma que `setAlwaysOnVpnPackage` está restringido a Device Owner/Profile Owner y exige un `VpnService` correctamente declarado. Fuente: [DevicePolicyManager](https://developer.android.com/reference/android/app/admin/DevicePolicyManager).
