# Simple Git Push Commands

## Always Push Local Changes (Overwrites Remote)

```bash
git add .
git commit -m "Update"
git push --force
```

## One-Liner (Copy & Paste)

```bash
git add . && git commit -m "Update" && git push --force
```

## If You Get Errors

```bash
git push --force-with-lease
```

