import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
BACKEND = os.path.join(ROOT, "backend")
DOCKER_RESOURCES = os.path.join(BACKEND, "src", "main", "resources")
FRONTEND = os.path.join(ROOT, "qhack-frontend")

commands = [
    (
        "gradlew build",
        [os.path.join(BACKEND, "gradlew"), "build"],
        BACKEND,
    ),
    ("docker compose up -d", ["docker", "compose", "up", "-d"], DOCKER_RESOURCES),
    ("npm install", ["npm", "i"], FRONTEND),
    ("npm run dev", ["npm", "run", "dev"], FRONTEND),
]

for label, cmd, cwd in commands:
    print(f"\n>>> {label} (in {cwd})\n", flush=True)
    result = subprocess.run(cmd, cwd=cwd)
    if result.returncode != 0:
        print(
            f"\n[ERROR] '{label}' failed with exit code {result.returncode}.",
            file=sys.stderr,
        )
        sys.exit(result.returncode)

print("\n[OK] All commands completed successfully.")
