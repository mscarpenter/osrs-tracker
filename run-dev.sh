#!/usr/bin/env bash
# Lança o cliente dev com as credenciais Jagex do Bolt.
# Abra o Bolt e logue na conta ANTES de rodar este script.

BOLT_JAGEX="/home/mateus-carpenter/.var/app/com.adamcake.Bolt/data/bolt-launcher/.runelite/jagex_cl_oldschool_LIVE.dat"
DEV_JAGEX="$HOME/.runelite/jagex_cl_oldschool_LIVE.dat"

if [ ! -f "$BOLT_JAGEX" ]; then
    echo "Arquivo Jagex não encontrado em Bolt. Abra o Bolt e logue primeiro."
    exit 1
fi

# Verifica se o token foi atualizado nos últimos 10 minutos
LAST_MOD=$(stat -c %Y "$BOLT_JAGEX")
NOW=$(date +%s)
AGE=$(( NOW - LAST_MOD ))

if [ "$AGE" -gt 600 ]; then
    echo "Aviso: token Jagex tem ${AGE}s — pode estar expirado."
    echo "Recomendado: feche o RuneLite do Bolt, reabra-o, logue e rode este script imediatamente."
    read -p "Continuar mesmo assim? (s/N) " -n 1 -r
    echo
    [[ ! $REPLY =~ ^[Ss]$ ]] && exit 1
fi

cp "$BOLT_JAGEX" "$DEV_JAGEX"
echo "Token copiado (${AGE}s de idade). Iniciando cliente dev..."

cd "$(dirname "$0")"
exec ./gradlew run
