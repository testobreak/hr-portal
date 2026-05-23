# repository/git_manager.py

import subprocess
from pathlib import Path
from typing import Optional


class GitManager:

    def __init__(
        self,
        root="."
    ):

        self.root = Path(root)

    # =====================================================
    # STATUS
    # =====================================================

    def status(self):

        result = subprocess.run(
            ["git", "status", "--short"],
            cwd=self.root,
            capture_output=True,
            text=True
        )

        return result.stdout

    # =====================================================
    # DIFF
    # =====================================================

    def diff(self):

        result = subprocess.run(
            ["git", "diff"],
            cwd=self.root,
            capture_output=True,
            text=True
        )

        return result.stdout

    # =====================================================
    # CURRENT BRANCH
    # =====================================================

    def current_branch(self):

        result = subprocess.run(
            ["git", "branch", "--show-current"],
            cwd=self.root,
            capture_output=True,
            text=True
        )

        return result.stdout.strip()

    # =====================================================
    # LAST COMMIT
    # =====================================================

    def last_commit(self):

        result = subprocess.run(
            ["git", "log", "-1", "--oneline"],
            cwd=self.root,
            capture_output=True,
            text=True
        )

        return result.stdout.strip()

    # =====================================================
    # CHECKPOINT
    # =====================================================

    def create_checkpoint(
        self,
        message: str = "agent checkpoint"
    ) -> Optional[str]:
        """
        Creates a lightweight recovery point before modifications.
        Returns the commit hash string if successful, or None.
        """
        try:

            subprocess.run(
                ["git", "add", "-A"],
                cwd=self.root,
                check=False
            )

            result = subprocess.run(
                [
                    "git",
                    "commit",
                    "-m",
                    message
                ],
                cwd=self.root,
                capture_output=True,
                text=True
            )

            if result.returncode != 0:
                return None

            return self.last_commit()

        except Exception:
            return None

    # =====================================================
    # COMMIT
    # =====================================================

    def commit(
        self,
        message: str
    ) -> bool:
        """
        Stages all untracked/modified files and runs a git commit.
        """
        try:

            subprocess.run(
                ["git", "add", "-A"],
                cwd=self.root,
                check=True
            )

            result = subprocess.run(
                [
                    "git",
                    "commit",
                    "-m",
                    message
                ],
                cwd=self.root,
                capture_output=True,
                text=True
            )

            return result.returncode == 0

        except Exception:
            return False

    # =====================================================
    # ROLLBACK FILE
    # =====================================================

    def rollback_file(
        self,
        file_path: str
    ) -> bool:
        """
        Reverts modifications to a single specified target file.
        """
        try:

            subprocess.run(
                [
                    "git",
                    "restore",
                    file_path
                ],
                cwd=self.root,
                check=True
            )

            return True

        except Exception:
            return False

    # =====================================================
    # ROLLBACK TO COMMIT
    # =====================================================

    def rollback_to_commit(
        self,
        commit_hash: str
    ) -> bool:
        """
        Performs a full hard reset to a specific previous commit anchor.
        """
        try:

            subprocess.run(
                [
                    "git",
                    "reset",
                    "--hard",
                    commit_hash
                ],
                cwd=self.root,
                check=True
            )

            return True

        except Exception:
            return False

    # =====================================================
    # GET LAST CHECKPOINT COMMIT
    # =====================================================

    def get_last_checkpoint(self) -> Optional[str]:
        """
        Finds the most recent checkpoint commit hash from the git log history.
        """
        try:
            result = subprocess.run(
                ["git", "log", "--grep=checkpoint", "-1", "--format=%H"],
                cwd=self.root,
                capture_output=True,
                text=True
            )
            commit_hash = result.stdout.strip()
            if commit_hash:
                return commit_hash
        except Exception:
            pass
        return None