#!/bin/bash
# Regenerates the MCP-named Minecraft jar (compile dependency) from the SRG jar.
# Usage: tools/regenerate-mcp-jar.sh
set -e
cd "$(dirname "$0")/.."
ASM=lib-repo/local/asm/7.1/asm-7.1.jar:lib-repo/local/asm-commons/7.1/asm-commons-7.1.jar:lib-repo/local/asm-tree/7.1/asm-tree-7.1.jar

# rebuild the tool if needed
javac -cp "$ASM" -d tools tools/MCRepack.java tools/TinyGen.java

java -cp "tools:$ASM" MCRepack lib/mc-1.12.2-srg.jar \
    tools/mappings/methods.csv tools/mappings/fields.csv srg2mcp \
    lib/mc-1.12.2-srg.jar lib/mc-1.12.2-mcp.jar

java -cp "tools:$ASM" MCRepack lib/mc-1.12.2-srg.jar \
    tools/mappings/methods.csv tools/mappings/fields.csv srg2mcp \
    lib/geckolib3-1.12.2.jar lib/geckolib3-1.12.2-mcp.jar

cp lib/mc-1.12.2-mcp.jar lib-repo/local/mc-1.12.2-mcp/1.0/mc-1.12.2-mcp-1.0.jar
cp lib/geckolib3-1.12.2-mcp.jar lib-repo/local/geckolib3-1.12.2-mcp/3.0/geckolib3-1.12.2-mcp-3.0.jar
echo "MCP jars regenerated."
