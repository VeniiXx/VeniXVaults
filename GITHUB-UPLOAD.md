[GITHUB-UPLOAD.md](https://github.com/user-attachments/files/30072787/GITHUB-UPLOAD.md)
# GitHub Upload Guide

## Recommended repository name

`VeniXVaults`

## Short description

Open-source personal vault plugin for Paper servers with GUI vaults, permissions, autosave, backups, blacklist, disabled worlds, and multi-language support.

## First upload with GitHub Desktop

1. Open GitHub Desktop.
2. Choose `File > Add local repository`.
3. Select this folder: `VeniXVaults-GitHub-Source`.
4. If GitHub Desktop asks, create the repository from this folder.
5. Commit with message: `Initial open-source release`.
6. Publish repository.

## First upload with command line

```bash
git init
git add .
git commit -m "Initial open-source release"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/VeniXVaults.git
git push -u origin main
```

## Build

```bash
mvn clean package
```

The compiled jar will be generated in `target/`.
