# Guia de Migração & Referência Técnica - Slimefun5 (Fork MaikonPlotegher)

Este guia reúne as **informações essenciais e práticas** para compilar, migrar e corrigir addons contra o fork oficial do **Slimefun5** em ambiente moderno (**Paper 1.21.x** e **Java 21/25**).

---

## 1. Dados do Fork & Instalação Local

* **Repositório GitHub:** `https://github.com/MaikonPlotegher/Slimefun5.git`
* **Branches:**
  * `main` — Versão estável (contém as tags de release como `v1.0`).
  * `development` — Branch para novas implementações e testes futuros.
* **Tag de Produção Atual:** `v1.0`

### Como instalar o fork no seu repositório Maven local (`~/.m2`):
Para permitir que qualquer projeto compile localmente sem depender de download do JitPack:
```bash
mvn install:install-file -Dfile="C:\Users\maiko\OneDrive\Desktop\servidor_26.1.2\plugins\Slimefun5-v1.0.jar" -DgroupId="com.github.MaikonPlotegher" -DartifactId="Slimefun5" -Dversion="v1.0" -Dpackaging="jar"
```

---

## 2. Como Configurar a Dependência nos Addons

### 2.1 No Maven (`pom.xml`)
```xml
<repositories>
    <!-- Repositório local (prioritário se você executou mvn install:install-file) -->
    <repository>
        <id>local-maven</id>
        <url>file://${user.home}/.m2/repository</url>
    </repository>
    <!-- Repositório JitPack (fallback remoto) -->
    <repository>
        <id>jitpack.io</id>
        <url>https://www.jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- Slimefun5 Fork (MaikonPlotegher) -->
    <dependency>
        <groupId>com.github.MaikonPlotegher</groupId>
        <artifactId>Slimefun5</artifactId>
        <version>v1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 2.2 No Gradle (`build.gradle.kts`)
```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.MaikonPlotegher:Slimefun5:v1.0")
}
```

---

## 3. Mudanças Obrigatórias na API do Slimefun5

| Recurso / Padrão | Slimefun4 (Legado) | Slimefun5 (Fork Atual) |
| :--- | :--- | :--- |
| **Pacote Base da API** | `io.github.thebusybiscuit.slimefun4.*` | `io.github.thebusybiscuit.slimefun5.*` |
| **Criação de `CustomItemStack`** | `new CustomItemStack(...)` | `CustomItemStack.create(...)` *(fábrica estática)* |
| **Uso em Receitas (`ItemStack[]`)** | `item` *(estendia ItemStack)* | `item.item()` *(extrai o ItemStack nativo)* |
| **Chaves de Identificação** | `org.bukkit.NamespacedKey` | `io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey` |
| **Classes Legadas `me.mrCookieSlime`** | Presentes | **100% Preservadas** (`BlockStorage`, `BlockMenu`, etc.) |

---

## 4. Regras Críticas para Paper 1.21+ e Java 25

1. **Atributos do Bukkit (`Attribute`):**
   * No 1.20.5+, `Attribute` deixou de ser enum e virou registro dinâmico (`Registry.ATTRIBUTE`).
   * **Nunca use:** `Attribute.valueOf(name)` ou `attribute.toString()` *(retorna string interna `CRAFTATTRIBUTE{...}` que quebra ao renascer mobs)*.
   * **Sempre use:** `attribute.getKey().toString()` para salvar, e `Registry.ATTRIBUTE.get(NamespacedKey)` para carregar.

2. **Remoção de Auto-Updaters Legados (`BlobBuildUpdater`):**
   * Deve ser removido do `onEnable()` de addons antigos. Ele tenta analisar a versão esperando um número de CI e quebra (`IllegalArgumentException: Not a valid build version`).
   * Fixe a versão estática no `plugin.yml`: `version: '1.0'`.

3. **Telemetria (`bStats`):**
   * Se o projeto não utilizar sombreamento funcional (`shadowJar` com relocate), remova a inicialização de `Metrics` para evitar `NoClassDefFoundError: org/bstats/bukkit/Metrics`.

4. **Incompatibilidade do Lombok com Java 25:**
   * O Lombok acessa campos internos do `javac` (`TypeTag::UNKNOWN`) que não existem no Java 25.
   * Remova o Lombok e declare getters e construtores privados manualmente.

5. **Codificação dos Arquivos Java:**
   * Arquivos `.java` devem ser salvos em **UTF-8 sem BOM**. O caractere BOM (`\ufeff`) faz o compilador falhar na primeira linha com erro de sintaxe.

---

## 5. Padrão de Transporte de Itens em Máquinas (`BlockMenuPreset`)

Em máquinas com múltiplos slots de entrada (como grids 3x3), o método `getSlotsAccessedByItemTransport` deve permitir o preenchimento de todos os slots:

```java
@Override
public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
    return flow == ItemTransportFlow.WITHDRAW ? getOutputSlots() : getInputSlots();
}

@Override
public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
    if (flow == ItemTransportFlow.WITHDRAW) {
        return getOutputSlots();
    }

    List<Integer> targetSlots = new LinkedList<>();
    List<Integer> emptySlots = new LinkedList<>();

    for (int slot : getInputSlots()) {
        ItemStack stack = menu.getItemInSlot(slot);
        if (stack == null || stack.getType().isAir()) {
            emptySlots.add(slot);
        } else if (SlimefunUtils.isItemSimilar(stack, item, false, true)) {
            if (stack.getAmount() < stack.getMaxStackSize()) {
                targetSlots.add(slot);
            }
        }
    }

    // 1º completa slots existentes do mesmo item; 2º preenche novos slots vazios
    targetSlots.sort(Comparator.comparingInt(s -> menu.getItemInSlot(s).getAmount()));
    targetSlots.addAll(emptySlots);

    return targetSlots.stream().mapToInt(Integer::intValue).toArray();
}
```
> [!IMPORTANT]
> Se o método retornar apenas os slots que já contêm o item, transportadores (como o *Network Pusher*) encherão apenas o 1º slot até 64 e pararão, deixando os demais slots permanentemente vazios.

---

## 6. Addons Atualizados e Padronizados (v1.0)

| Addon | Build Tool | Arquivo Gerado | Principais Ajustes Aplicados |
| :--- | :--- | :--- | :--- |
| **Slimefun5 (Core)** | Gradle 8.5 | `Slimefun5-v1.0.jar` | Fork oficial; correção do botão voltar no cheat mode; tags `v1.0`. |
| **ElectricSpawners** | Maven | `ElectricSpawners-v1.0.jar` | Migração para `slimefun5.*`, fábrica estática, dependência no fork. |
| **MobCapturer** | Gradle 9.3 | `MobCapturer-v1.0.jar` | Remoção do Lombok e bStats; resolução dinâmica de `Attribute` Paper 1.21+. |
| **SFCalc** | Gradle 9.3 | `SFCalc-v1.0.jar` | Interface gráfica (`/sfcalc`); cálculo de inventário; remoção de updaters legados. |
| **Supreme** | Maven | `Supreme-v1.0.jar` | Correção do transporte de itens para preencher os 9 slots; dependência no fork. |
