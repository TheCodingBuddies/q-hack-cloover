import os
import shutil
import subprocess
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
BACKEND = os.path.join(ROOT, "backend")
DOCKER_RESOURCES = os.path.join(BACKEND, "src", "main", "resources")
FRONTEND = os.path.join(ROOT, "qhack-frontend")


def run(label, cmd, cwd):
    print(f"\n>>> {label} (in {cwd})\n", flush=True)
    result = subprocess.run(cmd, cwd=cwd)
    if result.returncode != 0:
        print(
            f"\n[ERROR] '{label}' failed with exit code {result.returncode}.",
            file=sys.stderr,
        )
        sys.exit(result.returncode)


def clean():
    print("\n[CLEAN] Starting cleanup...\n", flush=True)

    # Stop and remove Docker containers
    run(
        "docker compose down",
        ["docker", "compose", "down", "--remove-orphans"],
        DOCKER_RESOURCES,
    )

    # Clean Gradle build
    run(
        "gradlew clean",
        [os.path.join(BACKEND, "gradlew"), "clean"],
        BACKEND,
    )

    # Remove node_modules
    node_modules = os.path.join(FRONTEND, "node_modules")
    if os.path.exists(node_modules):
        print(f"\n>>> Removing {node_modules}\n", flush=True)
        shutil.rmtree(node_modules)
        print(f"[OK] Removed {node_modules}")
    else:
        print(f"[SKIP] {node_modules} does not exist, nothing to remove.")

    print("\n[OK] Cleanup completed successfully.")


def start():
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
        run(label, cmd, cwd)

    print("\n[OK] All commands completed successfully.")


if __name__ == "__main__":
    if "--clean" in sys.argv:
        clean()
    else:
        start()
