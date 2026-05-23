# execution/test_runner.py

import subprocess

from pathlib import Path


class TestRunner:

    def __init__(self, root="."):

        self.root = Path(root)

    def run_backend_tests(self):

        return self.run_command(
            ["pytest"]
        )

    def run_frontend_tests(self):

        package_json = (
            self.root / "package.json"
        )

        if not package_json.exists():

            return {
                "success": False,
                "error":
                    "package.json not found"
            }

        return self.run_command(
            ["npm", "test", "--", "--run"]
        )

    def run_frontend_build(self):

        package_json = (
            self.root / "package.json"
        )

        if not package_json.exists():

            return {
                "success": False,
                "error":
                    "package.json not found"
            }

        return self.run_command(
            ["npm", "run", "build"]
        )

    def run_command(
        self,
        command,
        timeout=600
    ):

        try:

            result = subprocess.run(
                command,
                cwd=self.root,
                capture_output=True,
                text=True,
                timeout=timeout
            )

            return {
                "success":
                    result.returncode == 0,

                "return_code":
                    result.returncode,

                "stdout":
                    result.stdout,

                "stderr":
                    result.stderr
            }

        except subprocess.TimeoutExpired:

            return {
                "success": False,
                "error":
                    "Command timeout"
            }