import json
import sys

# Fix encoding for Windows
if sys.platform == 'win32':
    import codecs
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')

with open('docs/NPCList_OSRS.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

filtered = [n for n in data if 3300 <= n.get('x', 0) <= 3420 and 10050 <= n.get('y', 0) <= 10180]

by_name = {}
for n in filtered:
    name = n.get('name', 'Unknown')
    if name not in by_name:
        by_name[name] = []
    by_name[name].append(n)

output = f'Found {len(filtered)} NPCs\n\n'
for name, npcs in sorted(by_name.items()):
    npc_id = npcs[0].get('id', 0)
    output += f'{name} (ID: {npc_id}): {len(npcs)} spawns\n'
    for npc in npcs:
        output += f'  - ({npc.get("x", 0)}, {npc.get("y", 0)}, height {npc.get("p", 0)})\n'

print(output)

with open('wilderness_slayer_cave_npcs.json', 'w', encoding='utf-8') as f:
    json.dump(filtered, f, indent=2)

with open('wilderness_slayer_cave_output.txt', 'w', encoding='utf-8') as f:
    f.write(output)
