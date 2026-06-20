import os
import sys

try:
    import psycopg2
except ImportError:
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "psycopg2-binary"])
    import psycopg2

conn = psycopg2.connect("postgres://neondb_owner:npg_S0EB8OWyIqQT@ep-bold-bar-ai0b4cmm-pooler.c-4.us-east-1.aws.neon.tech/neondb?sslmode=require")
cur = conn.cursor()
cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'interviews';")
rows = cur.fetchall()
print("COLUMNS IN NEON DB:")
for row in rows:
    print(row[0])
cur.close()
conn.close()
