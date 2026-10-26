#!/usr/bin/env python3
"""
Script para actualizar automáticamente bot.py original de ToDus-bot-jk
con las funciones de vinculación directa para JK Downloader (sin alias y con datos verificados).
Uso:
    python3 patch_bot.py [ruta/al/bot.py]
"""
import sys
import shutil

target = sys.argv[1] if len(sys.argv) > 1 else "bot.py"

print(f"[*] Aplicando actualización a: {target}")

with open("release/bot.py", "r", encoding="utf-8") as src:
    content = src.read()

# Guardar copia de seguridad
try:
    shutil.copyfile(target, f"{target}.bak")
    print(f"[+] Respaldo guardado en: {target}.bak")
except Exception:
    pass

with open(target, "w", encoding="utf-8") as dst:
    dst.write(content)

print(f"[✓] ¡{target} actualizado con éxito! Ya puedes iniciar tu bot.")
