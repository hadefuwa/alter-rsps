#!/usr/bin/env python3
"""
Simple script to replace strings in a JAR file
No hex editor needed - works programmatically

Usage: python modify_jar_string.py <jar-file> <old-string> <new-string>
Example: python modify_jar_string.py libs/custom-runelite-client.jar "Alter" "PANDA SERVER"
"""

import sys
import zipfile
import shutil
from pathlib import Path

def modify_jar_string(jar_path, old_string, new_string):
    """Replace a string in a JAR file"""
    
    jar_file = Path(jar_path)
    if not jar_file.exists():
        print(f"Error: JAR file not found: {jar_path}")
        return False
    
    # For binary files (.class), strings must be same length
    # For text files, we can handle different lengths
    if len(old_string) != len(new_string):
        print(f"Warning: Strings have different lengths")
        print(f"  Old: '{old_string}' ({len(old_string)} chars)")
        print(f"  New: '{new_string}' ({len(new_string)} chars)")
        print(f"\nThis will work for text files, but may fail for binary .class files")
        print(f"Continuing anyway...")
        # Don't return False - try anyway for text files
    
    print(f"Modifying: {jar_path}")
    print(f"Replacing: '{old_string}' -> '{new_string}'")
    
    # Create backup
    backup_file = jar_file.with_suffix(jar_file.suffix + '.backup')
    if not backup_file.exists():
        shutil.copy2(jar_file, backup_file)
        print(f"Created backup: {backup_file.name}")
    
    # Create temporary JAR
    temp_jar = jar_file.with_suffix(jar_file.suffix + '.tmp')
    modified = False
    
    try:
        with zipfile.ZipFile(jar_file, 'r') as original_jar:
            with zipfile.ZipFile(temp_jar, 'w', zipfile.ZIP_DEFLATED) as new_jar:
                for entry in original_jar.namelist():
                    # Read the entry
                    data = original_jar.read(entry)
                    
                    # Try to modify text files (properties, txt, xml, json)
                    if entry.endswith(('.properties', '.txt', '.xml', '.json')):
                        try:
                            content = data.decode('utf-8')
                            if old_string in content:
                                content = content.replace(old_string, new_string)
                                data = content.encode('utf-8')
                                modified = True
                                print(f"  ✓ Modified: {entry}")
                        except UnicodeDecodeError:
                            pass
                    
                    # For .class files, only replace if same length (binary safe)
                    elif entry.endswith('.class'):
                        old_bytes = old_string.encode('utf-8')
                        new_bytes = new_string.encode('utf-8')
                        if len(old_bytes) == len(new_bytes) and old_bytes in data:
                            data = data.replace(old_bytes, new_bytes)
                            modified = True
                            print(f"  ✓ Modified (binary): {entry}")
                    
                    # Write entry to new JAR
                    new_jar.writestr(entry, data)
        
        if modified:
            # Replace original with modified
            shutil.move(temp_jar, jar_file)
            print(f"\n✅ Success! JAR file modified.")
            print(f"Next: Copy to RSProx and test")
            return True
        else:
            temp_jar.unlink()
            print(f"\n⚠️  String '{old_string}' not found in JAR file")
            print(f"Tip: The string might be encoded differently or not exist")
            return False
            
    except Exception as e:
        if temp_jar.exists():
            temp_jar.unlink()
        print(f"\n❌ Error: {e}")
        return False

if __name__ == '__main__':
    if len(sys.argv) < 4:
        print("Usage: python modify_jar_string.py <jar-file> <old-string> <new-string>")
        print('Example: python modify_jar_string.py libs/custom-runelite-client.jar "Alter" "PANDA SERVER"')
        sys.exit(1)
    
    success = modify_jar_string(sys.argv[1], sys.argv[2], sys.argv[3])
    sys.exit(0 if success else 1)










