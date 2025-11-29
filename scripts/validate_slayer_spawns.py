#!/usr/bin/env python3
"""
Slayer NPC Spawn Validator

This script scans all Kotlin spawn plugins to find which NPCs are spawned in the game,
and cross-references them with the npc.rscm file to get NPC IDs.

Run this script to see which NPCs are available for slayer tasks.

Usage:
    python scripts/validate_slayer_spawns.py
"""

import os
import re
from pathlib import Path
from collections import defaultdict

# Patterns to exclude from slayer tasks (same as in SlayerPlugin.kt)
EXCLUDED_NAME_PATTERNS = [
    "shop", "banker", "guard", "soldier", "knight", "wizard", "priest", "monk",
    "merchant", "trader", "farmer", "fisherman", "cook", "bartender", "nurse",
    "tutor", "master", "teacher", "guide", "leprechaun", "null", "spawn",
    "rock", "tentacle", "head", "wing", "twig", "pile",
    "giant skeleton", "zombie swab", "assassin", "assasin", "angry goblin", 
    "baboon thrall", "rebel warrior", "elidinis warden", "rooster", "mourner",
    "fear repear", "strangled", "prince itzla arkan"
]

def load_rscm_mappings(rscm_path):
    """Load NPC name to ID mappings from npc.rscm file."""
    mappings = {}
    try:
        with open(rscm_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if ':' in line:
                    name, npc_id = line.rsplit(':', 1)
                    try:
                        mappings[name.strip()] = int(npc_id.strip())
                    except ValueError:
                        pass
    except FileNotFoundError:
        print(f"Warning: Could not find {rscm_path}")
    return mappings

def find_spawn_plugins(game_plugins_path):
    """Find all Kotlin files that contain spawnNpc calls."""
    spawn_files = []
    for root, dirs, files in os.walk(game_plugins_path):
        for file in files:
            if file.endswith('.kt'):
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                        if 'spawnNpc' in content:
                            spawn_files.append(file_path)
                except Exception as e:
                    pass
    return spawn_files

def extract_spawned_npcs(file_path):
    """Extract NPC names from spawnNpc calls in a Kotlin file."""
    npcs = []
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
            # Pattern to match spawnNpc calls with npc parameter
            # Examples:
            # spawnNpc(npc = "npc.crawling_hand_448", x = 3420, ...)
            # spawnNpc("npc.shop_keeper", x = 3211, ...)
            patterns = [
                r'spawnNpc\s*\(\s*npc\s*=\s*"([^"]+)"',  # Named parameter
                r'spawnNpc\s*\(\s*"([^"]+)"',  # Positional parameter
            ]
            
            for pattern in patterns:
                matches = re.findall(pattern, content)
                for match in matches:
                    # Remove "npc." prefix if present
                    npc_name = match
                    if npc_name.startswith("npc."):
                        npc_name = npc_name[4:]
                    npcs.append(npc_name)
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
    
    return npcs

def is_valid_slayer_npc(npc_name):
    """Check if an NPC name is valid for slayer tasks."""
    name_lower = npc_name.lower().replace('_', ' ')
    
    # Check against excluded patterns
    for pattern in EXCLUDED_NAME_PATTERNS:
        if pattern in name_lower:
            return False
    
    return True

def main():
    # Get the project root directory
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    
    rscm_path = project_root / "data" / "cfg" / "rscm" / "npc.rscm"
    game_plugins_path = project_root / "game-plugins" / "src" / "main" / "kotlin"
    
    print("=" * 60)
    print("Slayer NPC Spawn Validator")
    print("=" * 60)
    print()
    
    # Load RSCM mappings
    print("Loading NPC mappings from npc.rscm...")
    rscm_mappings = load_rscm_mappings(rscm_path)
    print(f"  Loaded {len(rscm_mappings)} NPC mappings")
    print()
    
    # Find all spawn plugins
    print("Scanning for spawn plugins...")
    spawn_files = find_spawn_plugins(game_plugins_path)
    print(f"  Found {len(spawn_files)} files with spawnNpc calls")
    print()
    
    # Extract spawned NPCs
    all_spawned_npcs = defaultdict(list)  # npc_name -> list of file paths
    
    for file_path in spawn_files:
        npcs = extract_spawned_npcs(file_path)
        for npc in npcs:
            all_spawned_npcs[npc].append(file_path)
    
    print(f"Found {len(all_spawned_npcs)} unique NPC types spawned")
    print()
    
    # Separate valid slayer NPCs from excluded ones
    valid_slayer_npcs = {}
    excluded_npcs = {}
    unmapped_npcs = []
    
    for npc_name, files in all_spawned_npcs.items():
        if npc_name in rscm_mappings:
            npc_id = rscm_mappings[npc_name]
            if is_valid_slayer_npc(npc_name):
                valid_slayer_npcs[npc_name] = {
                    'id': npc_id,
                    'spawn_count': len(files)
                }
            else:
                excluded_npcs[npc_name] = {
                    'id': npc_id,
                    'spawn_count': len(files)
                }
        else:
            unmapped_npcs.append(npc_name)
    
    # Print valid slayer NPCs
    print("=" * 60)
    print(f"VALID SLAYER NPCs ({len(valid_slayer_npcs)} types)")
    print("=" * 60)
    for npc_name in sorted(valid_slayer_npcs.keys()):
        info = valid_slayer_npcs[npc_name]
        print(f"  {npc_name} (ID: {info['id']}) - {info['spawn_count']} spawn(s)")
    print()
    
    # Print excluded NPCs
    print("=" * 60)
    print(f"EXCLUDED NPCs ({len(excluded_npcs)} types)")
    print("=" * 60)
    for npc_name in sorted(excluded_npcs.keys()):
        info = excluded_npcs[npc_name]
        print(f"  {npc_name} (ID: {info['id']}) - {info['spawn_count']} spawn(s)")
    print()
    
    # Print unmapped NPCs
    if unmapped_npcs:
        print("=" * 60)
        print(f"UNMAPPED NPCs ({len(unmapped_npcs)} types)")
        print("These NPCs are spawned but not found in npc.rscm:")
        print("=" * 60)
        for npc_name in sorted(unmapped_npcs):
            print(f"  {npc_name}")
        print()
    
    # Generate a Kotlin-ready list of valid NPC IDs
    print("=" * 60)
    print("KOTLIN-READY VALID NPC IDs")
    print("Copy this to use as a whitelist if needed:")
    print("=" * 60)
    valid_ids = sorted([info['id'] for info in valid_slayer_npcs.values()])
    print(f"val validSlayerNpcIds = setOf(")
    for i, npc_id in enumerate(valid_ids):
        if i < len(valid_ids) - 1:
            print(f"    {npc_id},")
        else:
            print(f"    {npc_id}")
    print(")")
    print()
    
    # Summary
    print("=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print(f"  Total spawned NPC types: {len(all_spawned_npcs)}")
    print(f"  Valid for slayer tasks: {len(valid_slayer_npcs)}")
    print(f"  Excluded from slayer: {len(excluded_npcs)}")
    print(f"  Unmapped (not in RSCM): {len(unmapped_npcs)}")
    print()
    print("The SlayerPlugin now only assigns tasks for NPCs that are")
    print("actually spawned in your world!")

if __name__ == "__main__":
    main()
