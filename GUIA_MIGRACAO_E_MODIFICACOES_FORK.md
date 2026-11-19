# Guia Completo de Migração, Arquitetura & Uso da API - Slimefun5 (Fork MaikonPlotegher)

Este documento é a referência técnica oficial e definitiva para desenvolvedores que desejam **criar novos addons** ou **migrar addons legados do Slimefun4** para o fork moderno do **Slimefun5** em ambientes **Paper 1.20.5 até 1.26+** e **Java 21/25**.

---

## Sumário
1. [Visão Geral do Fork Slimefun5](#1-visão-geral-do-fork-slimefun5)
2. [O que mudou em relação ao Slimefun4 (Diferenças & Quebras)](#2-o-que-mudou-em-relação-ao-slimefun4-diferenças--quebras)
   - [2.1 Nomenclatura de Pacotes](#21-nomenclatura-de-pacotes)
   - [2.2 Desacoplamento de ItemStack (SlimefunItemStack e CustomItemStack)](#22-desacoplamento-de-itemstack-slimefunitemstack-e-customitemstack)
   - [2.3 Classes de Compatibilidade Bytecode (ASM Bridges me.mrCookieSlime)](#23-classes-de-compatibilidade-bytecode-asm-bridges-memrcookieslime)
   - [2.4 Nova Hierarquia de Máquinas (AbstractMachine & MachineStatus)](#24-nova-hierarquia-de-máquinas-abstractmachine--machinestatus)
   - [2.5 Correção de Spawners e Itens Distintos (DistinctiveItem)](#25-correção-de-spawners-e-itens-distintos-distinctiveitem)
   - [2.6 Preservação de Metadados em Saídas de Receitas (prepareCraftOutput)](#26-preservação-de-metadados-em-saídas-de-receitas-preparecraftoutput)
3. [Novo Sistema de Tradução & Pacotes (Packet Translation Layer)](#3-novo-sistema-de-tradução--pacotes-packet-translation-layer)
   - [3.1 Como Funciona a Renderização por Pacotes](#31-como-funciona-a-renderização-por-pacotes)
   - [3.2 Lore Estruturada em Blocos (LoreComposer)](#32-lore-estruturada-em-blocos-lorecomposer)
   - [3.3 Placeholders Dinâmicos em Tempo Real (DynamicLoreValues)](#33-placeholders-dinâmicos-em-tempo-real-dynamiclorevalues)
   - [3.4 Gerenciamento e Dessincronização de Encantamentos (EnchantDisplay)](#34-gerenciamento-e-dessincronização-de-encantamentos-enchantdisplay)
4. [Guia de Uso da API do Slimefun5 (Criando Addons)](#4-guia-de-uso-da-api-do-slimefun5-criando-addons)
   - [4.1 Configuração do Projeto (Maven e Gradle)](#41-configuração-do-projeto-maven-e-gradle)
   - [4.2 Registro de ItemGroup e Categorias Aninhadas](#42-registro-de-itemgroup-e-categorias-aninhadas)
   - [4.3 Criação e Registro de SlimefunItem](#43-criação-e-registro-de-slimefunitem)
   - [4.4 Criação de Máquinas Elétricas & Geradores de Energia](#44-criação-de-máquinas-elétricas--geradores-de-energia)
   - [4.5 Criação de Interfaces Gráficas (BlockMenu & BlockMenuPreset)](#45-criação-de-interfaces-gráficas-blockmenu--blockmenupreset)
   - [4.6 Regra Obrigatória para Transporte de Itens (ItemTransportFlow)](#46-regra-obrigatória-para-transporte-de-itens-itemtransportflow)
   - [4.7 Registro de Pesquisas (Research)](#47-registro-de-pesquisas-research)
   - [4.8 Persistência de Dados em Blocos (BlockStorage)](#48-persistência-de-dados-em-blocos-blockstorage)
5. [Diretrizes para Minecraft Moderno (Paper 1.20.5+ / 1.26+ e Java 25)](#5-diretrizes-para-minecraft-moderno-paper-1205--126-e-java-25)
   - [5.1 Atributos Dinâmicos (Registry.ATTRIBUTE)](#51-atributos-dinâmicos-registryattribute)
   - [5.2 Remoção Obrigatória do Lombok](#52-remoção-obrigatória-do-lombok)
   - [5.3 Remoção de Auto-Updaters Legados (BlobBuildUpdater)](#53-remoção-de-auto-updaters-legados-blobbuildupdater)
   - [5.4 Texturas de Cabeças (VersionedPlayerHead)](#54-texturas-de-cabeças-versionedplayerhead)
   - [5.5 Flags de Itens e Componentes (VersionedItemFlag)](#55-flags-de-itens-e-componentes-versioneditemflag)

---

## 1. Visão Geral do Fork Slimefun5

O **Slimefun5** (Fork MaikonPlotegher) é uma reestruturação do ecossistema do Slimefun focada em:
1. **Modernização Extrema:** Compatibilidade total com **Java 21 e Java 25**, suportando a arquitetura moderna de Data Components do Minecraft 1.20.5 até 1.26+.
2. **Eliminação de Mutações Globais:** No Slimefun4, templates de itens eram frequentemente mutados em tempo de execução, causando bugs de duplicação, perda de lore e corrupção de receitas. No Slimefun5, templates e instâncias de itens são estritamente isolados.
3. **Tradução Nativa em Camada de Rede (Netty / Packet-Level):** Internacionalização sem sobrescrever o inventário do servidor. Cada jogador enxerga os itens no seu próprio idioma (`pt-BR`, `en`, `es`, etc.) enquanto o servidor mantém identificadores canônicos consistentes.
4. **Compatibilidade com Addons Legados:** Através de injeção de bytecode (ASM), o Slimefun5 provê pontes para métodos históricos de classes como `me.mrCookieSlime.Slimefun.api.BlockStorage`, evitando que addons antigos quebrem por `NoSuchMethodError`.

* **Repositório GitHub:** `https://github.com/MaikonPlotegher/Slimefun5.git`
* **Branch Estável:** `main` | **Branch de Desenvolvimento:** `development`
* **Tag Atual:** `v1.0`

---

## 2. O que mudou em relação ao Slimefun4 (Diferenças & Quebras)

Esta seção lista as quebras de compatibilidade direta que qualquer desenvolvedor encontrará ao migrar um plugin do Slimefun4 para o Slimefun5.

### 2.1 Nomenclatura de Pacotes
Todas as classes do pacote base foram renomeadas de `slimefun4` para `slimefun5`:
```
Slimefun4: io.github.thebusybiscuit.slimefun4.*
Slimefun5: io.github.thebusybiscuit.slimefun5.*
```
> [!TIP]
> Em seus projetos legados, um simples *Search and Replace* em todo o projeto substituindo `io.github.thebusybiscuit.slimefun4` por `io.github.thebusybiscuit.slimefun5` resolve a imensa maioria dos imports quebrados.

### 2.2 Desacoplamento de ItemStack (SlimefunItemStack e CustomItemStack)
No Slimefun4, classes como `CustomItemStack` e `SlimefunItemStack` estendiam diretamente `org.bukkit.inventory.ItemStack`. Isso gerava um problema crítico: qualquer chamada acidental a `item.setAmount()` ou `item.getItemMeta().setDisplayName(...)` alterava o template global do item registrado no servidor!

No Slimefun5:
1. **Fábrica Estática em `CustomItemStack`:**
   ```java
   // SLIMEFUN4 (Inválido no SF5):
   ItemStack item = new CustomItemStack(Material.DIAMOND, "&bSuper Diamante", "&7Lore");

   // SLIMEFUN5 (Correto):
   ItemStack item = CustomItemStack.create(Material.DIAMOND, "&bSuper Diamante", "&7Lore");
   ```
2. **Extração de `ItemStack` via `.item()`:**
   `SlimefunItemStack` encapsula o item base. Para passá-lo em receitas ou métodos Bukkit que exigem `ItemStack`:
   ```java
   // SLIMEFUN4:
   RecipeType.ENHANCED_CRAFTING_TABLE.register(new ItemStack[] { SlimefunItems.STEEL_INGOT, ... }, output);

   // SLIMEFUN5:
   RecipeType.ENHANCED_CRAFTING_TABLE.register(new ItemStack[] { SlimefunItems.STEEL_INGOT.item(), ... }, output.item());
   ```

### 2.3 Classes de Compatibilidade Bytecode (ASM Bridges me.mrCookieSlime)
O pacote legado `me.mrCookieSlime.*` foi mantido para retrocompatibilidade, mas modernizado:
- No Slimefun4, addons antigos chamavam frequentemente `BlockStorage.check(Block)` ou `BlockStorage.check(Location)`.
- No Slimefun5, o pipeline do Gradle (via ASM no `:core:classes`) injeta automaticamente os métodos pontelegados na classe compilada de `BlockStorage`, garantindo que addons legados compilados contra o SF4 não quebrem por `NoSuchMethodError` ao rodar no SF5.

### 2.4 Nova Hierarquia de Máquinas (AbstractMachine & MachineStatus)
No Slimefun4, quase todas as máquinas estendiam `AContainer`, que possuía código procedural antigo para manipulação de menus e inventários.

No Slimefun5:
- Foi introduzida a classe abstrata `AbstractMachine` (`io.github.thebusybiscuit.slimefun5.implementation.items.electric.AbstractMachine`).
- `AContainer` agora herda de `AbstractMachine`.
- **Estados Visuais Padronizados (`MachineStatus`):** A máquina gerencia dinamicamente se está `PRODUCING`, `IDLE` ou `NOT_ENOUGH_ENERGY`.
- A API gráfica disponibiliza slots de status prontos via `ChestMenuUtils.getMachineStatus(...)` (utilizando painéis de vidro colorido temáticos, como vidro preto para IDLE).

### 2.5 Correção de Spawners e Itens Distintos (DistinctiveItem)
No Slimefun4, itens com o mesmo ID Slimefun mas dados internos diferentes (como o `REPAIRED_SPAWNER` ou `BROKEN_SPAWNER`, que possuem a entidade gravada no NBT/PDC) eram frequentemente tratados como idênticos nas mesas de trabalho.
- **No Slimefun5:** As classes de bancadas (`EnhancedCraftingTable`, `MagicWorkbench`, `AncientAltar`) agora habilitam `checkDistinction = true`.
- Spawners implementam a interface `DistinctiveItem`, impedindo que uma receita de spawner de vaca aceite acidentalmente um spawner de blaze ou esqueleto.

### 2.6 Preservação de Metadados em Saídas de Receitas (prepareCraftOutput)
No Slimefun4, o processamento de saídas em máquinas como `AutoEnchanter`, `AutoDisenchanter` ou fornos industriais frequentemente sobrescrevia o item de saída com o template cru do item (`output.clone()`), limpando encantamentos, durabilidade, tags de PDC ou dados de reparo.
- **No Slimefun5:** O método `prepareCraftOutput(ItemStack output)` protege itens que possuam encantamentos, `StoredEnchantments`, níveis de reparo (`RepairCost`), dano personalizado ou itens recarregáveis (`Rechargeable`), mantendo todas as modificações do jogador intactas.

---

## 3. Novo Sistema de Tradução & Pacotes (Packet Translation Layer)

Uma das maiores inovações arquiteturais do Slimefun5 é a separação entre **dados do servidor** e **exibição do cliente**.

### 3.1 Como Funciona a Renderização por Pacotes
Em vez de gravar nomes e lores fixas em inglês ou português dentro do arquivo de salvamento do mundo (`world/data`), o Slimefun5 mantém os dados canônicos e utiliza interceptação de pacotes no Netty (`PacketTranslationService` e `PacketItemRewriter`).
1. O servidor guarda o item com seu ID Slimefun e dados essenciais.
2. Quando a janela de inventário é transmitida para o jogador via rede, o Slimefun intercepta o pacote `PacketPlayOutSetSlot` / `PacketPlayOutWindowItems`.
3. O item é reescrito na memória de rede de acordo com o idioma do jogador (`Player#getLocale()`), lendo os arquivos `languages/<lang>/items.yml`.

### 3.2 Lore Estruturada em Blocos (LoreComposer)
No Slimefun4, as lores eram listas lineares de strings concatenadas em Java. No Slimefun5, a classe `LoreComposer` estrutura a lore em 4 categorias modulares definidas no YAML:
```yaml
COBALT_PICKAXE:
  name: '&bPicareta de Cobalto'
  type:
    - '&7&oFerramenta'
  description:
    - '&fUma picareta de altíssima velocidade.'
  stats:
    - '&8⇨ &e⚡ &7Consumo: 10 J/uso'
  usage:
    - '&eClique Direito &7para ativar o modo super minerador.'
```
A composição cuida automaticamente da indentação, das cores padrão e da eliminação de linhas vazias duplicadas via `normalizeBlankLines`.

### 3.3 Placeholders Dinâmicos em Tempo Real (DynamicLoreValues)
Itens com atributos dinâmicos (como carga elétrica de jetpacks ou usos restantes em itens limitados) não precisam mais re-gerar a lore inteira a cada tick.
O Slimefun5 reserva tokens nos templates YAML:
* `%charge%` e `%max_charge%` (para itens que implementam `Rechargeable`)
* `%uses%` e `%max_uses%` (para itens derivados de `LimitedUseItem`)

A classe `DynamicLoreValues.substitute` faz a substituição instantânea em memória durante o disparo do pacote para o cliente, sem tocar no cache estático do template.

### 3.4 Gerenciamento e Dessincronização de Encantamentos (EnchantDisplay)
No Slimefun5, itens que já nascem encantados por padrão (como a `Cobalt Pickaxe` com Eficiência VI e Inquebrável X) possuem seus encantamentos reposicionados na categoria `type` da lore, ocultando o cabeçalho nativo do Minecraft com `ItemFlag.HIDE_ENCHANTS`.

#### As Duas Correções Críticas Implementadas no Fork:
1. **Tradução Dinâmica na Camada de Pacote (`adjustEnchantmentLore`):**
   Se o jogador aplicar novos encantamentos (via `AutoEnchanter`) ou desencantar o item (via `AutoDisenchanter`), o `ItemTranslationService` detecta que os encantamentos daquele `ItemStack` diferem do template registrado. Ele remove cirurgicamente as linhas de encantamento do template e renderiza as linhas correspondentes aos encantamentos atuais.
2. **Remoção de Linhas Físicas na Desencantadora (`removeEnchantmentLore`):**
   Ao passar um item com encantamentos pré-definidos no `AutoDisenchanter`, a máquina chama `EnchantDisplay.removeEnchantmentLore(itemMeta, enchant, level)` e `EnchantDisplay.reveal(itemMeta)`. Isso limpa o texto fixado na lore física do item no Bukkit e restaura a visibilidade de encantamentos nativos, eliminando completamente o bug de "encantamento fantasma visual".

---

## 4. Guia de Uso da API do Slimefun5 (Criando Addons)

### 4.1 Configuração do Projeto

#### No Maven (`pom.xml`):
```xml
<repositories>
    <repository>
        <id>local-maven</id>
        <url>file://${user.home}/.m2/repository</url>
    </repository>
    <repository>
        <id>jitpack.io</id>
        <url>https://www.jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.MaikonPlotegher</groupId>
        <artifactId>Slimefun5</artifactId>
        <version>v1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### No Gradle (`build.gradle.kts`):
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

### 4.2 Registro de ItemGroup e Categorias Aninhadas
Para que os itens do seu addon apareçam organizados no Guia do Slimefun (`/sf guide`):

```java
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

public class MyAddonGroups {

    public static ItemGroup MAIN_CATEGORY;
    public static ItemGroup SUB_CATEGORY;

    public static void setup(Plugin plugin) {
        // Categoria Principal
        NamespacedKey mainKey = new NamespacedKey(plugin, "main_category");
        SlimefunItemStack mainIcon = new SlimefunItemStack("MY_MAIN_ICON", Material.NETHER_STAR, "&6Meu Super Addon");
        MAIN_CATEGORY = new ItemGroup(mainKey, mainIcon.item(), 3);

        // Subcategoria aninhada
        NamespacedKey subKey = new NamespacedKey(plugin, "sub_machines");
        SlimefunItemStack subIcon = new SlimefunItemStack("MY_SUB_ICON", Material.FURNACE, "&eMáquinas Avançadas");
        SUB_CATEGORY = new ItemGroup(subKey, subIcon.item(), MAIN_CATEGORY, 1);

        MAIN_CATEGORY.register(plugin);
        SUB_CATEGORY.register(plugin);
    }
}
```

### 4.3 Criação e Registro de SlimefunItem
Para criar itens regulares, consumíveis ou materiais de craft:

```java
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MyCustomItems {

    public static SlimefunItemStack CRISTAL_ENERGETICO;

    public static void setup(MyPlugin plugin) {
        CRISTAL_ENERGETICO = new SlimefunItemStack(
            "CRISTAL_ENERGETICO",
            Material.AMETHYST_SHARD,
            "&dCristal Energético",
            "&7Um cristal condensado com energia pura."
        );

        ItemStack[] receita = new ItemStack[] {
            new ItemStack(Material.DIAMOND), new ItemStack(Material.REDSTONE), new ItemStack(Material.DIAMOND),
            new ItemStack(Material.REDSTONE), new ItemStack(Material.NETHER_STAR), new ItemStack(Material.REDSTONE),
            new ItemStack(Material.DIAMOND), new ItemStack(Material.REDSTONE), new ItemStack(Material.DIAMOND)
        };

        SlimefunItem item = new SlimefunItem(
            MyAddonGroups.SUB_CATEGORY,
            CRISTAL_ENERGETICO,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            receita
        );

        item.register(plugin);
    }
}
```

### 4.4 Criação de Máquinas Elétricas & Geradores de Energia
Para criar uma máquina integrada à rede de energia (`EnergyNet`):

```java
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun5.core.networks.energy.EnergyNetComponentType;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import org.bukkit.inventory.ItemStack;

public class TrituradorEletrico extends AContainer implements EnergyNetComponent {

    public static final int ENERGY_CONSUMPTION = 32;
    public static final int CAPACITY = 128;

    public TrituradorEletrico(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER; // Use GENERATOR para produtores ou CAPACITOR para baterias
    }

    @Override
    public int getCapacity() {
        return CAPACITY;
    }

    @Override
    public int getEnergyConsumption() {
        return ENERGY_CONSUMPTION;
    }

    @Override
    public int getSpeed() {
        return 1;
    }

    @Override
    protected void registerDefaultRecipes() {
        // Registra as receitas internas da máquina
        registerRecipe(4, new ItemStack[] { new ItemStack(org.bukkit.Material.COBBLESTONE) },
                          new ItemStack[] { new ItemStack(org.bukkit.Material.GRAVEL) });
    }

    @Override
    public String getMachineIdentifier() {
        return "TRITURADOR_ELETRICO";
    }
}
```

### 4.5 Criação de Interfaces Gráficas (BlockMenu & BlockMenuPreset)
Para criar máquinas com menus personalizados e suporte completo à automação:

```java
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import io.github.thebusybiscuit.slimefun5.utils.ChestMenuUtils;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class CustomMachineMenuSetup {

    private static final int[] INPUT_SLOTS = new int[] { 19, 20 };
    private static final int[] OUTPUT_SLOTS = new int[] { 24, 25 };
    private static final int STATUS_SLOT = 22;

    public static void createPreset(String id, String title) {
        new BlockMenuPreset(id, title) {
            @Override
            public void init() {
                // Desenha bordas de fundo
                drawBackground(ChestMenuUtils.getInputSlotTexture(), new int[] { 10, 11, 12, 28, 29, 30 });
                drawBackground(ChestMenuUtils.getOutputSlotTexture(), new int[] { 14, 15, 16, 32, 33, 34 });

                // Botão de status central
                addItem(STATUS_SLOT, ChestMenuUtils.getMachineStatus(false), ChestMenuUtils.getEmptyClickHandler());
            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {
                // Configurações específicas por instância do bloco no mundo
            }

            @Override
            public boolean canOpen(Block b, org.bukkit.entity.Player p) {
                return true;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return flow == ItemTransportFlow.WITHDRAW ? OUTPUT_SLOTS : INPUT_SLOTS;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
                // REGRA CRÍTICA DE TRANSPORTE (Ver Seção 4.6)
                if (flow == ItemTransportFlow.WITHDRAW) {
                    return OUTPUT_SLOTS;
                }
                return getTargetSlotsForInput(menu, INPUT_SLOTS, item);
            }
        };
    }
}
```

### 4.6 Regra Obrigatória para Transporte de Itens (ItemTransportFlow)
> [!CAUTION]
> **A causa número 1 de bugs em máquinas multi-slot:** Se o seu método de transporte de itens retornar apenas slots que já contenham o item, transportadores automáticos (como o *Network Pusher* ou *Cargo Nodes*) encherão o primeiro slot até 64 itens e travarão a máquina, deixando todos os outros slots de entrada vazios para sempre!

Sempre implemente a lógica de distribuição balanceada:

```java
import io.github.thebusybiscuit.slimefun5.utils.SlimefunUtils;
import java.util.*;

public static int[] getTargetSlotsForInput(DirtyChestMenu menu, int[] inputSlots, ItemStack item) {
    List<Integer> targetSlots = new LinkedList<>();
    List<Integer> emptySlots = new LinkedList<>();

    for (int slot : inputSlots) {
        ItemStack stack = menu.getItemInSlot(slot);
        if (stack == null || stack.getType().isAir()) {
            emptySlots.add(slot);
        } else if (SlimefunUtils.isItemSimilar(stack, item, false, true)) {
            if (stack.getAmount() < stack.getMaxStackSize()) {
                targetSlots.add(slot);
            }
        }
    }

    // 1º: Preenche slots que já possuem o mesmo item (ordenados por menor quantidade)
    targetSlots.sort(Comparator.comparingInt(s -> menu.getItemInSlot(s).getAmount()));
    // 2º: Se não houver mais espaço nos existentes, preenche os slots vazios
    targetSlots.addAll(emptySlots);

    return targetSlots.stream().mapToInt(Integer::intValue).toArray();
}
```

### 4.7 Registro de Pesquisas (Research)
Para vincular itens a pesquisas que exigem níveis de experiência:

```java
import io.github.thebusybiscuit.slimefun5.api.researches.Research;
import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class MyResearches {

    public static void setup(Plugin plugin) {
        NamespacedKey researchKey = new NamespacedKey(plugin, "pesquisa_energia_avancada");
        // ID único numérico (ex: 7800), Nome legível, Custo em níveis de EXP
        Research research = new Research(researchKey, 7800, "Energia Quântica", 25);

        // Vincula os itens que serão desbloqueados por esta pesquisa
        research.addItems(MyCustomItems.CRISTAL_ENERGETICO);

        research.register();
    }
}
```

### 4.8 Persistência de Dados em Blocos (BlockStorage)
O `BlockStorage` armazena metadados de blocos Slimefun diretamente no disco sem necessidade de NBT no bloco:

```java
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;

// Salvar metadados em uma localização:
BlockStorage.addBlockInfo(loc, "energy-charge", String.valueOf(charge), false);
BlockStorage.addBlockInfo(loc, "owner", player.getUniqueId().toString(), false);

// Recuperar metadados:
String chargeStr = BlockStorage.getLocationInfo(loc, "energy-charge");
int currentCharge = chargeStr != null ? Integer.parseInt(chargeStr) : 0;

// Verificar se um bloco é um item Slimefun específico:
if (BlockStorage.check(loc, "TRITURADOR_ELETRICO")) {
    // Bloco válido
}
```

---

## 5. Diretrizes para Minecraft Moderno (Paper 1.20.5+ / 1.26+ e Java 25)

### 5.1 Atributos Dinâmicos (Registry.ATTRIBUTE)
No Minecraft 1.20.5+, a classe `org.bukkit.attribute.Attribute` **deixou de ser um enum** e passou a ser uma interface de registros dinâmicos.
```java
// ERRADO (Lança IllegalArgumentException ou gera String inválida no 1.21+):
Attribute attr = Attribute.valueOf("GENERIC_MAX_HEALTH");
String name = attr.toString(); // Retorna "CRAFTATTRIBUTE{key=minecraft:generic.max_health}" e quebra ao carregar!

// CORRETO (Compatível com 1.20.5 até 1.26+):
// 1. Para salvar como chave Namespaced:
String key = attr.getKey().toString(); // "minecraft:generic.max_health"

// 2. Para carregar do registro:
Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.fromString(key));
```

### 5.2 Remoção Obrigatória do Lombok
> [!WARNING]
> No **Java 25**, o compilador `javac` removeu campos internos que o Lombok manipulava via reflexão (`com.sun.tools.javac.code.TypeTag::UNKNOWN`). Se o seu addon contiver anotações `@Getter`, `@Setter` ou `@RequiredArgsConstructor` do Lombok, a compilação falhará imediatamente.
- **Ação:** Remova o Lombok do `pom.xml` / `build.gradle` e declare getters, setters e construtores de forma padrão em Java.

### 5.3 Remoção de Auto-Updaters Legados (BlobBuildUpdater)
Addons legados do SF4 frequentemente continham chamadas como:
```java
new BlobBuildUpdater(this, getFile(), "MaikonPlotegher/MeuAddon", "development").start();
```
O serviço BlobBuild não existe mais e o validador de versões tenta fazer parse de números inteiros de CI, disparando `IllegalArgumentException: Not a valid build version` e impedindo o plugin de inicializar.
- **Ação:** Remova completamente a inicialização do updater no método `onEnable()`. Fixe a versão estática no `plugin.yml` (`version: '1.0'`).

### 5.4 Texturas de Cabeças (VersionedPlayerHead)
No 1.20.5+, o sistema de GameProfile e Data Components mudou radicalmente o modo como skins e crânios de blocos são serializados.
- **No Slimefun5:** Utilize a classe utilitária:
  ```java
  import io.github.thebusybiscuit.slimefun5.utils.compatibility.VersionedPlayerHead;

  // Aplica textura de skin por Hash de forma segura em qualquer versão:
  VersionedPlayerHead.setSkinFromHash(block, textureUUID, textureHash, false);
  ```

### 5.5 Flags de Itens e Componentes (VersionedItemFlag)
No Minecraft 1.21.5+, a Mojang moveu flags como `HIDE_ENCHANTS` e `HIDE_ATTRIBUTES` para o componente de dados `tooltip_display`. Constantes do `ItemFlag` foram deprecadas ou removidas.
- **No Slimefun5:** Nunca chame `ItemMeta#addItemFlags(ItemFlag.HIDE_ENCHANTS)` diretamente.
- Utilize sempre `VersionedItemFlag`:
  ```java
  import io.github.thebusybiscuit.slimefun5.utils.compatibility.VersionedItemFlag;

  // Adicionar flags com segurança contra null na versão atual:
  VersionedItemFlag.addFlags(meta, VersionedItemFlag.HIDE_ATTRIBUTES, VersionedItemFlag.HIDE_ENCHANTS);

  // Remover flags:
  VersionedItemFlag.removeFlags(meta, VersionedItemFlag.HIDE_ENCHANTS);
  ```

---

## 6. Resumo Rápido para Migração de Addons Legados

1. **Atualize o pom.xml / build.gradle:** Troque a dependência de `com.github.Slimefun:Slimefun4` para `com.github.MaikonPlotegher:Slimefun5:v1.0`.
2. **Substitua Pacotes:** Renomeie imports de `slimefun4` para `slimefun5`.
3. **Substitua `new CustomItemStack`:** Use `CustomItemStack.create(...)`.
4. **Extraia `ItemStack`:** Adicione `.item()` onde instâncias de `SlimefunItemStack` forem passadas em arrays de receitas.
5. **Remova Updaters:** Delete qualquer referência a `BlobBuildUpdater` ou `GitHubBuildsUpdater`.
6. **Remova Lombok:** Se for compilar no Java 25, remova o plugin do Lombok e declare os métodos em código nativo.
7. **Verifique o Transporte de Itens:** Garanta que suas máquinas com menus implementem a ordenação e preenchimento de slots vazios no método `getSlotsAccessedByItemTransport`.
8. **Compile & Deploy:** Instale o jar gerado na pasta `plugins/` e inicie o servidor com **Java 21/25**.
