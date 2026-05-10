---
description: Add or update the Spotless profile in a module's pom.xml following ikara-saas conventions. Usage: /spotless-config <module-path>
arguments:
  - name: module-path
    description: "Relative path to the module (e.g. core, commons, security, eureka). Defaults to current module if omitted."
    required: false
---

Add or regenerate the `spotless` Maven profile in the target module's `pom.xml` using the ikara-saas standard template.

## Steps

1. Read `$module-path/pom.xml` (or ask user which module if not provided).
2. Detect module type from existing deps/plugins:
   - **service** — has `spring-boot-maven-plugin` → include `<yaml>` section
   - **lib** — no `spring-boot-maven-plugin` → omit `<yaml>` section
   - **jooq** — has `jooq-codegen-maven` plugin or `jooq` dep → add `<excludes>` for generated code
3. Check which properties already exist, using these names:
   - `package.o.spotless.version` — Spotless plugin version
   - `package.o.palantir-java.format.version` — Palantir formatter version
   - `dir.main.java` — main sources dir
   - `dir.test.java` — test sources dir
   - `dir.resources` — resources dir (only needed if yaml section present)
   If any are missing, add them to `<properties>` with the standard defaults below.
4. If a `<profile><id>spotless</id>` block already exists, replace it entirely. Otherwise append it before `</profiles>` (create `<profiles>` if absent).
5. Confirm the edit with the user before writing.

## Standard Property Defaults

```
package.o.spotless.version          = 3.4.0
package.o.palantir-java.format.version = 2.90.0
dir.main.java                       = src/main/java
dir.test.java                       = src/test/java
dir.resources                       = src/main/resources
```

## Spotless Profile Template

```xml
<profile>
    <id>spotless</id>
    <build>
        <plugins>
            <plugin>
                <groupId>com.diffplug.spotless</groupId>
                <artifactId>spotless-maven-plugin</artifactId>
                <version>${package.o.spotless.version}</version>
                <configuration>
                    <java>
                        <includes>
                            <include>${dir.main.java}/**/*.java</include>
                            <include>${dir.test.java}/**/*.java</include>
                        </includes>
                        <!-- JOOQ_EXCLUDES: only if module has jOOQ codegen -->
                        <excludes>
                            <exclude>${dir.main.java}/**/jooq/**/*.java</exclude>
                            <exclude>${dir.main.java}/**/generated/**/*.java</exclude>
                        </excludes>
                        <!-- END_JOOQ_EXCLUDES -->
                        <palantirJavaFormat>
                            <version>${package.o.palantir-java.format.version}</version>
                            <style>PALANTIR</style>
                        </palantirJavaFormat>
                        <importOrder/>
                        <removeUnusedImports>
                            <engine>cleanthat-javaparser-unnecessaryimport</engine>
                        </removeUnusedImports>
                        <formatAnnotations/>
                        <trimTrailingWhitespace/>
                        <endWithNewline/>
                        <toggleOffOn/>
                    </java>
                    <pom>
                        <includes>
                            <include>pom.xml</include>
                        </includes>
                        <sortPom>
                            <encoding>UTF-8</encoding>
                            <expandEmptyElements>false</expandEmptyElements>
                            <keepBlankLines>false</keepBlankLines>
                            <nrOfIndentSpace>4</nrOfIndentSpace>
                            <sortDependencies>scope,groupId,artifactId</sortDependencies>
                            <sortDependencyExclusions>groupId,artifactId</sortDependencyExclusions>
                            <sortDependencyManagement>groupId,artifactId</sortDependencyManagement>
                            <sortExecutions>false</sortExecutions>
                            <sortModules>true</sortModules>
                            <sortPlugins>groupId,artifactId</sortPlugins>
                            <sortProperties>true</sortProperties>
                        </sortPom>
                    </pom>
                    <!-- YAML_SECTION: only if module is a service (has spring-boot-maven-plugin) -->
                    <yaml>
                        <includes>
                            <include>${dir.resources}/*.yml</include>
                            <include>${dir.resources}/*.yaml</include>
                        </includes>
                        <jackson>
                            <features>
                                <INDENT_OUTPUT>true</INDENT_OUTPUT>
                                <ORDER_MAP_ENTRIES_BY_KEYS>true</ORDER_MAP_ENTRIES_BY_KEYS>
                            </features>
                            <yamlFeatures>
                                <WRITE_DOC_START_MARKER>true</WRITE_DOC_START_MARKER>
                                <MINIMIZE_QUOTES>true</MINIMIZE_QUOTES>
                                <SPLIT_LINES>true</SPLIT_LINES>
                            </yamlFeatures>
                        </jackson>
                        <trimTrailingWhitespace/>
                        <endWithNewline/>
                    </yaml>
                    <!-- END_YAML_SECTION -->
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>com.diffplug.spotless</groupId>
                        <artifactId>spotless-maven-plugin</artifactId>
                        <version>${package.o.spotless.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>com.palantir.javaformat</groupId>
                        <artifactId>palantir-java-format</artifactId>
                        <version>${package.o.palantir-java.format.version}</version>
                    </dependency>
                </dependencies>
                <executions>
                    <execution>
                        <goals>
                            <goal>check</goal>
                            <goal>apply</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

## Known Pitfalls (must avoid)

- **Never** add `<endWithNewline>` or `<indentAttribute>` inside `<sortPom>` — Spotless 2.x/3.x throws "Cannot find 'endWithNewline' in SortPom".
- **Never** use plain `<removeUnusedImports/>` — always include `<engine>cleanthat-javaparser-unnecessaryimport</engine>`.
- `<yaml>` section requires `dir.resources` property — add it to `<properties>` if missing.
- `<excludes>` block inside `<java>` is only for modules with jOOQ codegen — omit for libs without it.

## After Editing

Tell the user:
```
Run: mvn spotless:apply -P spotless   (from the module directory)
Run: mvn spotless:check -P spotless   (to verify)
```
