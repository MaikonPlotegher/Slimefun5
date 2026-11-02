# Guia de Desenvolvimento & Migração de Addons para Slimefun5 (v1.0)

Este relatório serve como referência técnica para atualizar, compilar e migrar seus addons (como *InfinityExpansion, SlimeTinker, ExtraTools, Networks, FastMachines, AlchimiaVitae*, etc.) para a nova arquitetura do **Slimefun5-v1.0**.

---

## 1. O Que Mudou entre Slimefun4 e Slimefun5?

| Conceito | Slimefun4 (Legado) | Slimefun5 (v1.0 Atual) |
| :--- | :--- | :--- |
| **Pacote Base da API** | `io.github.thebusybiscuit.slimefun4.*` | `io.github.thebusybiscuit.slimefun5.*` |
| **Classes Legadas `me.mrCookieSlime`** | Presentes (`BlockStorage`, `BlockMenu`, etc.) | **100% Preservadas** para compatibilidade com addons |
| **Registro de DisplayName e Lore** | Aplicado direto no `ItemMeta` | **Restaurado!** O construtor clássico grava direto no item |
| **Tradução por Pacotes (Netty)** | Inexistente | **Desativada** por padrão (zero bugs de rede e texturas) |
| **Identificação do Item** | NBT / PDC (`slimefun:slimefun_item`) | PDC nativo Paper/Spigot via `CustomItemDataService` |
| **Versão Java Mínima** | Java 8 / 16 / 17 | Bytecode Java 8 com suporte a JVM 21 e 25 |

---

## 2. Como Configurar o Addon para Compilar contra o Slimefun5

Para compilar os addons sem depender do JitPack ou de repositórios remotos desatualizados, a melhor prática é apontar para a versão local do **`Slimefun5-v1.0.jar`**.

### 2.1 No Maven (`pom.xml`)
Exemplo (usado no *ExtraTools, AlchimiaVitae, CercaLaco*):

```xml
<dependencies>
    <!-- Paper API 1.21.x -->
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>26.1.2.build.74-stable</version>
        <scope>provided</scope>
    </dependency>

    <!-- Slimefun5 Core (v1.0) -->
    <dependency>
        <groupId>com.github.Slimefun5.Slimefun5</groupId>
        <artifactId>core</artifactId>
        <version>v1.0</version>
        <scope>provided</scope>
        <!-- Se estiver usando arquivo local na pasta /libs do projeto: -->
        <systemPath>${basedir}/libs/Slimefun5-v1.0.jar</systemPath>
    </dependency>
</dependencies>
```

> [!TIP]
> **Instalando no repositório local do Maven**: Você também pode rodar uma vez no terminal:
> ```bash
> mvn install:install-file -Dfile="C:\Users\maiko\OneDrive\Desktop\servidor_26.1.2\plugins\Slimefun5-v1.0.jar" -DgroupId=com.github.Slimefun5.Slimefun5 -DartifactId=core -Dversion=v1.0 -Dpackaging=jar
> ```
> Feito isso, qualquer `pom.xml` compilará com `<version>v1.0</version>` e `<scope>provided</scope>` sem precisar de `<systemPath>`.

### 2.2 No Gradle (`build.gradle.kts` ou `build.gradle`)
Exemplo (usado no *InfinityExpansion, SlimeTinker, SlimeFrame*):

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    
    // Slimefun5 v1.0 local:
    compileOnly(files("../../Slimefun5/core/build/libs/Slimefun5-v1.0.jar"))
}

tasks.shadowJar {
    // REGRA CRÍTICA: NUNCA empacotar classes do Slimefun dentro do addon!
    exclude("io/github/thebusybiscuit/slimefun5/**")
    exclude("io/github/thebusybiscuit/slimefun4/**")
    exclude("me/mrCookieSlime/**")
}
```

---

## 3. Como Declarar Itens nos Addons (Método Clássico)

Com a arquitetura que atualizamos hoje, você **não precisa** forçar o addon a usar arquivos externos de pacote se não quiser. O método tradicional do Slimefun4 voltou a funcionar:

### 3.1 Método 1: Hardcoded / Clássico no Java
```java
public static final SlimefunItemStack MEU_ITEM = new SlimefunItemStack(
    "MEU_ITEM_ID",
    Material.DIAMOND,
    "&bDiamante Energizado",
    "&7Um diamante infundido com",
    "&7energia cósmica.",
    "",
    "&e+100 de Carga"
);
```
- O nome e a lore agora são **gravados diretamente no `ItemMeta`**.
- O ID `MEU_ITEM_ID` é carimbado no PDC (`slimefun:slimefun_item`).
- Funciona perfeitamente em baús, inventários, receitas e na mão do jogador.

### 3.2 Método 2: Via Arquivo de Tradução do Addon (Opcional)
Se o addon tiver seu próprio arquivo `languages/pt-BR/items.yml` ou `messages.yml`:
- Você pode criar o item apenas com `new SlimefunItemStack("MEU_ITEM_ID", Material.DIAMOND);`
- E no `items.yml` do addon:
  ```yaml
  MEU_ITEM_ID:
    name: '&bDiamante Energizado'
    description:
    - '&7Um diamante infundido com'
    - '&7energia cósmica.'
  ```
- O Slimefun lerá o arquivo do addon e aplicará o texto diretamente no item durante o boot do servidor.

---

## 4. Substituição de Imports e Classes Chave

Ao atualizar o código Java de um addon do Slimefun4 para o Slimefun5:

| Import Antigo (Slimefun4) | Import Novo (Slimefun5) |
| :--- | :--- |
| `io.github.thebusybiscuit.slimefun4.api.SlimefunAddon` | `io.github.thebusybiscuit.slimefun5.api.SlimefunAddon` |
| `io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem` | `io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem` |
| `io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack` | `io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack` |
| `io.github.thebusybiscuit.slimefun4.api.items.ItemGroup` | `io.github.thebusybiscuit.slimefun5.api.items.ItemGroup` |
| `io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType` | `io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType` |
| `io.github.thebusybiscuit.slimefun4.api.researches.Research` | `io.github.thebusybiscuit.slimefun5.api.researches.Research` |
| `io.github.thebusybiscuit.slimefun4.implementation.Slimefun` | `io.github.thebusybiscuit.slimefun5.implementation.Slimefun` |
| `io.github.thebusybiscuit.slimefun4.core.attributes.*` | `io.github.thebusybiscuit.slimefun5.core.attributes.*` |
| `me.mrCookieSlime.Slimefun.api.BlockStorage` | **Mantido igual** (`me.mrCookieSlime.Slimefun.api.BlockStorage`) |
| `me.mrCookieSlime.Slimefun.api.inventory.BlockMenu` | **Mantido igual** (`me.mrCookieSlime.Slimefun.api.inventory.BlockMenu`) |

> [!NOTE]
> Se alguma biblioteca antiga ou código de terceiros ainda chamar `io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem`, o nosso jar do **Slimefun5-v1.0** possui a classe shim e a ponte em bytecode injetada, evitando falhas de `NoClassDefFoundError` ou `NoSuchMethodError`.

---

## 5. Cuidados no Minecraft 1.20.5+ e 1.21.4+ (Paper API)

1. **`InventoryView` (Mudança do Paper 1.21)**:
   - Em versões antigas: `player.getOpenInventory().getTitle()`
   - No Paper 1.21+: o método `getTitle()` foi descontinuado ou substituído por `title()` (Component) ou métodos de Paper. Para compatibilidade entre versões, utilize reflexão ou acesse o título através do preset do menu.
2. **`HeadTexture` / Skulls**:
   - Para definir cabeças customizadas (como em geradores e mochilas):
     Use `SlimefunItemStack(id, "texture_hash_hex")` ou `CustomItemStack(Material.PLAYER_HEAD, skullOwner)`.
     Evite manipular GameProfile por NBT bruto na 1.21+, pois o Paper agora utiliza `PlayerProfile` nativo.
3. **Leitura do ID do Slimefun**:
   - Nunca verifique `item.getItemMeta().getDisplayName()` para saber se o item é do Slimefun!
   - Use sempre:
     ```java
     SlimefunItem sfItem = SlimefunItem.getByItem(item);
     if (sfItem != null) {
         String id = sfItem.getId();
         // item legítimo do Slimefun
     }
     ```

---

## 6. Integração com Máquinas Virtuais e GriefDefender

Se o seu addon cria máquinas com inventário próprio (ex: *FastMachines, InfinityExpansion, Networks*):
* Sempre crie o inventário herdando de `BlockMenuPreset` ou `AContainer`.
* O GriefDefender agora detecta essas máquinas automaticamente como contêineres e protege os baús/menus contra invasores dentro de terrenos claimados através do método:
  ```java
  BlockMenuPreset.isInventory(id)
  ```
  Isso garante que todas as máquinas dos seus addons recebam proteção total contra jogadores sem permissão.

---

## 7. Normalização e Migração Segura de Itens (`ItemCanonicalizer`)

### 7.1 A Regra Estrita do ID Bruto
No modelo anterior com injeção de pacotes NMS, o servidor mantinha itens no mundo com o `display_name` igual ao ID bruto (ex: `COPPER_DUST`) e sem lore.
Para migrar esses itens antigos sem quebrar a personalização do servidor:
* O `ItemCanonicalizer` agora **apenas atua** se o `display_name` atual do item for **estritamente igual ao seu ID bruto** (`id.equals(currentName)`).
* **Itens Renomeados por Admins (ex: `/rename`)**: Como o novo nome é diferente do ID bruto, o normalizador **nunca toca** nesses itens ao serem dropados ou puxados por funis.
* **Itens Renomeados na Bigorna**: Continuam protegidos pela flag `slimefun:renamed = "1"`.
* **Equipamentos e Máquinas**: Itens com `maxStackSize <= 1` continuam 100% imunes ao normalizador.

> [!NOTE]
> **Efeito Dominó Premeditado**: Caso algum item legítimo no chão ainda esteja com o ID bruto da época do NMS, ele será migrado automaticamente para o template de nome e lore do servidor no momento em que for dropado ou recolhido por um jogador ou funil. Itens já no formato do servidor ou customizados não sofrem nenhuma alteração.

---

## 8. Contêineres Vanilla sem NMS (`VanillaContainerTitleListener` Removido)

Nas versões anteriores com NMS, o Slimefun apagava o nome de blocos com inventário vanilla (`clearInheritedCustomName` no `BlockListener`) para forçar o envio de pacotes traduzidos por jogador.
* **O que foi corrigido**:
  1. O método `clearInheritedCustomName` foi **removido**. Ao colocar uma Fornalha Melhorada no mundo, o Minecraft vanilla grava o nome do Slimefun nativamente no bloco (`CustomName`).
  2. O `VanillaContainerTitleListener` foi **desativado**.
  3. Quando qualquer jogador abre o contêiner, o próprio Paper/Minecraft envia a janela nativamente com o nome do Slimefun já definido pelo servidor, com zero atraso de ticks e sem qualquer injeção NMS.

---

## 9. Modernização do PDC (PersistentDataContainer)

* O `PdcCompat` foi atualizado para invocar a interface nativa `PersistentDataContainer` diretamente sem `ReflectionCompat`.
* O método `getKeys()` foi adicionado aos stubs de compilação, permitindo checagens de integridade de tags de forma direta e sem impacto de performance no tick do servidor.

---

## 10. Nova API de Status de Máquinas e Barra de Progresso (`MachineStatus` & `ChestMenuUtils`)

Muitos addons de Slimefun sofriam com UIs inconsistentes em máquinas que usavam painéis de vidro estáticos com `&aProcessing`, sem indicar tempo restante, porcentagem ou motivo de paradas (falta de energia, saída cheia, etc.).

Para padronizar e modernizar as máquinas em todos os addons, foram adicionados o enum [`MachineStatus`](file:///core/src/main/java/io/github/thebusybiscuit/slimefun5/core/machines/MachineStatus.java) e métodos utilitários em [`ChestMenuUtils`](file:///core/src/main/java/io/github/thebusybiscuit/slimefun5/utils/ChestMenuUtils.java).

### 10.1 Estados Padronizados (`MachineStatus`)
* `MachineStatus.IDLE` (Vidro Preto): Máquina ociosa / aguardando itens.
* `MachineStatus.PROCESSING` (Vidro Verde-Lima): Em operação. Suporta barra de progresso dinâmica, tempo restante em segundos/ticks e porcentagem.
* `MachineStatus.NOT_ENOUGH_ENERGY` (Vidro Vermelho): Falta de energia. Mostra a energia atual vs. a necessária (ex: `&4⚡ &c20 &7/ &c100 J`).
* `MachineStatus.OUTPUT_FULL` (Vidro Laranja): Saída cheia / sem espaço no inventário.
* `MachineStatus.NO_FUEL` (Vidro Amarelo): Sem combustível.
* `MachineStatus.NO_WATER` (Vidro Azul Claro): Sem água.
* `MachineStatus.DISABLED` (Vidro Preto): Máquina desligada.

### 10.2 Exemplos de Uso em Addons

#### 1. Atualizando barra de progresso em tempo real:
```java
// remainingTicks: tempo restante, totalTicks: tempo total da receita
ChestMenuUtils.updateStatus(menu, STATUS_SLOT, ChestMenuUtils.getProcessingStatus(remainingTicks, totalTicks));
```
Isso gera automaticamente um vidro verde-lima com:
```
&aEm Operação
&7Progresso: &a■■■■■■■■■■&8■■■■■■■■■■ &f50%
&7Tempo Restante: &e2.5s
```

#### 2. Indicando falta de energia detalhada:
```java
int storedEnergy = getCharge(b.getLocation());
int neededEnergy = getEnergyConsumption();
ChestMenuUtils.updateStatus(menu, STATUS_SLOT, ChestMenuUtils.getNotEnoughEnergyStatus(storedEnergy, neededEnergy));
```
Isso gera:
```
&cSem Energia Suficiente
&7Armazenado: &4⚡ &c15 &7/ &c50 J
```

#### 3. Otimização de Performance Nula (Zero Overhead):
O método `ChestMenuUtils.updateStatus(menu, slot, item)` verifica internamente `menu.hasViewer()`. Se nenhum jogador estiver com o menu aberto, **nenhuma atualização de inventário ou pacote de rede é enviado**, garantindo custo computacional insignificante no tick do servidor.

---

## 11. Nova Classe Base de Máquinas (`AbstractMachine`) e Ponte Legada (`AContainer`)

Para modernizar o ecossistema e eliminar a necessidade de cada addon criar sua própria classe base de máquina, foi introduzida a classe [`AbstractMachine`](file:///core/src/main/java/io/github/thebusybiscuit/slimefun5/core/machines/AbstractMachine.java).

### 11.1 Recursos da `AbstractMachine`
* **Localização Oficial**: Pacote moderno `io.github.thebusybiscuit.slimefun5.core.machines.AbstractMachine`.
* **Slot de Status Customizável**: O método `getStatusSlot()` tem como padrão o slot `22`, mas pode ser sobrescrito por qualquer máquina para posicionar o progresso em qualquer slot da UI.
* **Fast-Path Ocioso (`isInputEmpty`)**: Quando a máquina não possui itens nas entradas, ela pula a iteração pesada de receitas, poupando CPU no tick assíncrono.
* **Integração Nativa com `MachineStatus`**: Gerencia automaticamente os estados `IDLE`, `PROCESSING` (barra dinâmica), `NOT_ENOUGH_ENERGY` e `OUTPUT_FULL`.
* **Encadeamento Fluente com Inferência de Tipo**: Métodos como `.setCapacity(int)`, `.setProcessingSpeed(int)` e `.setEnergyConsumption(int)` retornam a própria classe filha sem necessidade de casts.

### 11.2 Ponte Legada com `AContainer` (Retrocompatibilidade 100%)
A classe legada [`AContainer`](file:///core/src/main/java/me/mrCookieSlime/Slimefun/Objects/SlimefunItem/abstractItems/AContainer.java) agora estende diretamente a `AbstractMachine`.
* **Addons Antigos**: Continuam funcionando normalmente sem mexer em nenhuma linha de código e herdam automaticamente todas as otimizações e o novo visual de status.
* **Addons Novos / Modernizados**: Devem estender diretamente `AbstractMachine`.
* **Configuração de Avisos no Console**: Adicionada a opção `options.warn-on-deprecated-classes: false` no `config.yml`. Por padrão em `false`, o console de inicialização do servidor não fica poluído com avisos de depreciação repetitivos para cada máquina registrada. Se um administrador desejar auditar classes legadas em uso, basta mudar para `true`.

---

## 12. Categorias Hierárquicas em Múltiplos Níveis e Correção do `/sf cheat`

### 12.1 Modo Clássico por Padrão (`categorize-main-menu: false`)
* A configuração `guide.categorize-main-menu` no [`config.yml`](file:///core/src/main/resources/config.yml) e em [`MainMenuLayoutOption`](file:///core/src/main/java/io/github/thebusybiscuit/slimefun5/core/guide/options/MainMenuLayoutOption.java) agora é **`false` por padrão**.
* O Guia do Slimefun abre no modo clássico (lista plana e direta das categorias dos addons e do Slimefun) para todos os jogadores sem precisar ajustar opções manualmente.

### 12.2 Correção da Bagunça no `/sf cheat` (`CheatSheetSlimefunGuide`)
* **O Bug Original**: O `/sf cheat` ignorava a visibilidade das categorias e ocultava os `NestedItemGroup`s (pais), despejando todas as `SubItemGroup`s (filhos) soltas diretamente na tela inicial do cheat.
* **A Correção**: O `/sf cheat` agora herda a coleta limpa e hierárquica do Guia normal. As categorias-pai aparecem organizadas na raiz; ao clicar, abre-se a hierarquia interna; e ao clicar em um item final, ele é entregue diretamente ao inventário do administrador.
* **Histórico de Navegação no Cheat**: O botão "Voltar" agora funciona no modo cheat, retornando um nível na árvore sem resetar tudo para o menu principal.

### 12.3 Suporte a Múltiplos Níveis (Pai ➔ Filho ➔ Neto ➔ Bisneto)
A classe [`NestedItemGroup`](file:///core/src/main/java/io/github/thebusybiscuit/slimefun5/api/items/groups/NestedItemGroup.java) agora suporta aninhamento em profundidade arbitrária:
```java
// Nível 1: Pai (Aparece no menu principal do guia)
NestedItemGroup pai = new NestedItemGroup(new NamespacedKey(addon, "categoria_pai"), iconePai);

// Nível 2: Filho (Fica dentro do Pai)
NestedItemGroup filho = new NestedItemGroup(new NamespacedKey(addon, "categoria_filho"), pai, iconeFilho);

// Nível 3: Neto (Fica dentro do Filho)
NestedItemGroup neto = new NestedItemGroup(new NamespacedKey(addon, "categoria_neto"), filho, iconeNeto);

// Nível 4: Bisneto / Folha (Contém os itens finais)
SubItemGroup bisneto = new SubItemGroup(new NamespacedKey(addon, "categoria_bisneto"), neto, iconeBisneto);
```
* Qualquer `NestedItemGroup` que tenha um `parent` é ocultado do menu principal automaticamente e exibido apenas dentro de seu respectivo grupo pai.
* Suporta 100% de compatibilidade binária com os construtores e métodos existentes de addons legados.


---

## 13. Diagnóstico e Correção de Receitas em Addons (Caso FastMachines)

### 13.1 O Problema
No addon **FastMachines**, ao colocar materiais nos slots de entrada (primeiras 4 linhas, slots 0-35), os slots de visualização (slots 36-41 e 45-50) permaneciam como painéis de vidro vazios e **nenhuma receita possível era exibida**.

### 13.2 As Causas Raiz Identificadas
1. **Falha Crítica no `PersistentDataContainer.equals()` (`MetaCompat.java`)**:
   * O método `MetaCompat.metaDiffers` executava:
     ```java
     if (!a.getPersistentDataContainer().equals(b.getPersistentDataContainer())) {
         return true;
     }
     ```
   * **Por que quebrava**: Na API do Bukkit/Paper, a classe de implementação `CraftPersistentDataContainer` **não implementa `equals(Object)`** (ela herda `Object.equals`, que compara referência de ponteiro de memória `==`). Como `ItemMeta` cria novas instâncias a cada chamada, `containerA.equals(containerB)` **sempre retornava `false`**.
   * Consequentemente, `metaDiffers` sempre retornava `true`, e `ItemExt.isSimilarTo` sempre retornava `false` para qualquer item que tivesse metadata em servidores 1.14+.
2. **Ordem Incorreta de Comparação de Itens Slimefun (`ItemExt.java`)**:
   * A verificação de ID do Slimefun (`getItemDataService().getItemData(...)`) estava posicionada *depois* da checagem de meta. O código descartava os itens antes mesmo de verificar se ambos eram itens do Slimefun com o mesmo identificador.
3. **Inconsistência no `Comparable` de `ItemWrapper`**:
   * O método `ItemWrapper.compareTo` utilizava apenas hashes de identidade arbitrários quando itens não eram similares, quebrando o ordenamento em coleções `TreeMap` usadas para agrupar entradas de receitas.

### 13.3 A Solução Aplicada
1. **Remoção do `PersistentDataContainer.equals()`** em `MetaCompat.java`.
2. **Priorização da Comparação por ID de Slimefun** em `ItemExt.java`:
   * Se ambos os itens forem Slimefun, eles são validados diretamente por seus IDs (e `DistinctiveItem.canStack` se aplicável), sem passar por heurísticas frágeis de NBT/PDC genérico.
   * Se apenas um for item do Slimefun e o outro for vanilla, a comparação rejeita imediatamente com `false`.
3. **Ordenamento Total Estável no `ItemWrapper`**:
   * Agora compara tipo (`Material`), ID do Slimefun e display name antes de qualquer fallback de identidade.
4. **Declaração de `RecipeDisplayItem` na `AbstractMachine`**:
   * `AbstractMachine` agora implementa formalmente `RecipeDisplayItem`, garantindo que loaders de receitas e o guia do Slimefun reconheçam qualquer máquina derivada sem necessidade de declaração manual.

---

## 13. SlimeTinker: Correção dos Elos de Malha (`PART_MAIL_LINKS`) e Cores/Formatos de Materiais

### 13.1 O Problema
Ao fundir/criar a peça de armadura de elos de malha (`MAIL_LINKS`) na fundição (`TinkersSmeltery`):
1. O item saía com `"Error"` no título e `"§7Error"` na lore.
2. As peças criadas pelo molde não refletiam a cor nem a formatação de estilo (negrito/itálico) e traço do material, pois a renderização dinâmica por pacotes foi desativada no Slimefun 5 e as instâncias das peças dependiam do template estático em cache.

### 13.2 Causas Raiz Identificadas
1. **Definição estática no `Parts.java`:**
   Enquanto todas as outras peças eram declaradas via `new BaseItem("PART_...", SkullTextures....)`, `PART_MAIL_LINKS` estava declarada como:
   ```java
   public static final SlimefunItemStack PART_MAIL_LINKS = ThemeUtils.themedItemStack(
       "PART_MAIL_LINKS",
       SkullTextures.PART_LINKS,
       ThemeItemType.PART,
       DESC_ERROR,
       ThemeUtils.PASSIVE + DESC_ERROR
   );
   ```
   onde `DESC_ERROR = "Error"`.
2. **Falta da chave `PART_MAIL_LINKS` nos arquivos de idiomas:**
   Existia apenas a entrada estática de guia `PART_MAIL_LINKS_DUMMY` em `languages/pt-BR/items.yml` e `languages/en/items.yml`.
3. **Template de peças sem montagem de nome e lore no `getStack`:**
   Em `PartTemplate.java`, `getStack(...)` gravava apenas as tags PDC (`PART_MATERIAL`, `PART_CLASS`, `PART_TYPE`), deixando o nome e a lore em branco (esperando uma renderização por pacote via `TinkerItemResolver`).
4. **Falta de traço em `ItemUtils.buildPartStats`:**
   `buildPartStats` inseria apenas a linha `"Material : <material>"`, sem indicar o traço concedido por aquela peça específica (`TraitPartType`).
5. **Formatação de traço sem estilos:**
   `formatPropertyName` usava `TinkerMaterialManager.getColorById(s)` (apenas ChatColor) em vez de `getMaterialFormatting(s)` (que preserva a cor hexadecimal e estilos como negrito).

### 13.3 Soluções Implementadas
1. **`Parts.java`:**
   `PART_MAIL_LINKS` alterado para `new BaseItem("PART_MAIL_LINKS", SkullTextures.PART_LINKS)`.
2. **`languages/pt-BR/items.yml` & `languages/en/items.yml`:**
   Adicionada a entrada `PART_MAIL_LINKS` com o nome traduzido e descrição padrão de peça.
3. **`PartTemplate.java` & `RepairkitTemplate.java`:**
   No método `getStack(...)`, compõe imediatamente o `displayName` (com a cor/formatação do material via `ItemUtils.formatMaterialName(material, lang)` + `ThemeUtils.ITEM_PART` + nome traduzido da peça) e constrói a lore completa com tipo de componente, descrição e `buildPartStats`.
4. **`ItemUtils.java`:**
   - Em `buildPartStats`, mapeia `PART_CLASS` para o `TraitPartType` correspondente (`HEAD`, `BINDER`, `ROD`, `PLATES`, `GAMBESON`, `LINKS`) e anexa a linha do traço com a formatação e tradução corretas.
   - Em `formatPropertyName`, usa `getMaterialFormatting(s)` garantindo a cor hexadecimal e estilos.
5. **`TinkersSmelteryCache.java`:**
   Garante a higienização de qualquer lore residual com `"Error"`, define o nome composto traduzido caso ausente/errôneo e anexa os atributos do material.
6. **Deploy:**
   Projeto compilado com sucesso e `SlimeTinker-v1.0.jar` implantado em `C:\Users\maiko\OneDrive\Desktop\servidor_26.1.2\plugins\`.

---

## 14. Padronização de Versionamento (`v1.0`)

Para assegurar rastreabilidade e saber exatamente quais addons já foram adaptados e testados para o **Slimefun 5 (Paper 1.21.4)**:
* Todos os addons adaptados passam a ter sua versão base definida como **`v1.0`** em seus arquivos de build (`build.gradle.kts` / `pom.xml`) e manifesto (`plugin.yml`).
* Os artefatos finais gerados são nomeados no padrão `<NomeDoAddon>-v1.0.jar`.
* Novos ajustes e correções incrementais seguirão o padrão semântico (`v1.0.1`, `v1.0.2`, `v1.1`, etc.).
* **Addons já padronizados nesta versão:**
  * `Slimefun5` $\rightarrow$ `Slimefun5-v1.0.jar`
  * `FastMachines` $\rightarrow$ `FastMachines-v1.0.jar`
  * `SlimeTinker` $\rightarrow$ `SlimeTinker-v1.0.jar`
  * `FNAmplifications` $\rightarrow$ `FNAmplifications-v1.0.jar`
  * `Networks` $\rightarrow$ `Networks-v1.0.jar`
  * `FoxyMachines` $\rightarrow$ `FoxyMachines-v1.0.jar`

---

## 15. Lore Dinâmica e Placeholders em Addons (`decorateBakedTemplate`)

### 15.1 O Problema dos Placeholders Não Substituídos
Em addons como o **FN-FAL-s-Amplifications**, itens como Gemas, Unbinders, Cajados e Quebradores Elétricos possuem linhas na lore baseadas em configurações do addon (como `% de chance` por nível, `max-uses` ou `tickrate`).
Nos arquivos de idiomas (`languages/pt-BR/items.yml`), essas linhas vêm com placeholders estáticos como:
```yaml
FN_GEM_ACHILLES_HEEL:
  description:
    - "&6⨳ Nível I: &e%"
    - "&6⨳ Nível II: &e%"
    - "&6⨳ Nível III: &e%"
    - "&6⨳ Nível IV: &e%"
```
Quando o Slimefun 5 registra o item, o serviço de tradução (`ItemTranslationService.canonicalizeToId`) executa `item.bakeTranslatedDisplay(name, lore)`, que sobrescreve a lore do `ItemStack` com o texto estático do YAML. Se a substituição dinâmica tiver sido feita apenas no construtor do Java, ela é apagada pelo processo de tradução, fazendo com que o item apareça no jogo com o `%` literal em vez dos números (ex.: `2%`, `3%`, `5%`, `10%`).

### 15.2 A Solução: Hook `decorateBakedTemplate`
O Slimefun 5 Core fornece um hook protegido em `SlimefunItem`:
```java
protected void decorateBakedTemplate(@Nonnull ItemMeta meta) {
    // no-op por padrão
}
```
Esse hook é invocado **no final** de `bakeTranslatedDisplay`, permitindo que subclasses processem e preencham qualquer dado dinâmico diretamente no `ItemMeta` antes que o template seja gravado.

### 15.3 Implementação no Addon
1. Criar sobrecargas nos utilitários de formatação para aceitar diretamente `ItemMeta`:
   ```java
   public static void setGemTierLore(ItemMeta meta, String configSection, String configSectionSetting, String stringToReplace, String color, String suffix, int tier, String fileName);
   public static void setLoreByConfigValue(@Nonnull ItemMeta meta, String configSection, String configSectionSetting, String stringToReplace, String color, String suffix, String fileName);
   ```
2. Sobrescrever `decorateBakedTemplate` nas classes base dos itens:
   * **`AbstractGem`:**
     ```java
     @Override
     protected void decorateBakedTemplate(@Nonnull ItemMeta meta) {
         int chanceVal = this.chance;
         if (chanceVal <= 0) {
             chanceVal = FNAmplifications.getInstance().getConfigManager().getCustomConfig("gem-settings").getInt(this.getId() + ".chance", 0);
             this.chance = chanceVal;
         }
         if (chanceVal > 0) {
             Utils.setGemTierLore(meta, this.getId(), "chance", "%", "&e", "%", 4, "gem-settings");
         }
     }
     ```
   * **`AbstractGemUnbinder`:**
     ```java
     @Override
     protected void decorateBakedTemplate(@Nonnull ItemMeta meta) {
         Utils.setLoreByConfigValue(meta, this.getId(), "unbind-chance", "%", "&e", "%", "unbind-gem-settings");
     }
     ```
   * **`AbstractStaff`:**
     ```java
     @Override
     protected void decorateBakedTemplate(@Nonnull ItemMeta meta) {
         Utils.setLoreByConfigValue(meta, this.getId(), "max-uses", "left", "&e", " left", "staffs-settings");
     }
     ```
   * **`ElectricBlockBreaker` & `CustomMaterialGenerator`:**
     ```java
     @Override
     protected void decorateBakedTemplate(@Nonnull ItemMeta meta) {
         Utils.setLoreByConfigValue(meta, this.getId(), "tickrate", "ticks", "&e", " ticks", "block-breaker-tickrate");
     }
     ```
Dessa forma, sempre que as traduções forem aplicadas ou recarregadas, os placeholders são substituídos dinamicamente pelos valores reais calculados.

---

## 16. Correção do Network Rake (`NetworkBestPusher` / `Network Advanced Pusher`)

### 16.1 O Problema
No addon **Networks**, a ferramenta **Network Rake** (usada para desmantelar nós da rede com um clique) falhava ao tentar quebrar o **Network Advanced Pusher** (`NTW_BEST_PUSHER` / `NetworkBestPusher`).

### 16.2 Causa Raiz
Em `NetworkRake.java`, a validação dos blocos viáveis utilizava:
```java
viableObjects.contains(slimefunItem.getClass())
```
onde `viableObjects` era um `Set<Class<? extends NetworkObject>>`.
Dois fatores causavam a falha:
1. `NetworkBestPusher.class` não havia sido adicionado explicitamente ao conjunto `viableObjects`.
2. A comparação usava `Set.contains(Class<?>)`, que requer igualdade exata de classe e falha para qualquer subclasse (ex.: `NetworkBestPusher extends NetworkPusher`).

### 16.3 Solução Implementada
1. `NetworkBestPusher.class` adicionado ao `viableObjects`.
2. Criado método `isViable(Class<?> clazz)` que itera com `viable.isAssignableFrom(clazz)`:
   ```java
   public boolean isViable(@Nonnull Class<?> clazz) {
       for (Class<? extends NetworkObject> viable : viableObjects) {
           if (viable.isAssignableFrom(clazz)) {
               return true;
           }
       }
       return false;
   }
   ```
3. O addon foi compilado e padronizado na versão `v1.0` gerando `Networks-v1.0.jar`.

---

## 17. Correção da Runa Inquebrável (`FoxyMachines` / `UnbreakableRune`)

### 17.1 O Problema
No addon **FoxyMachines**, a **Runa Inquebrável** (`UNBREAKABLE_RUNE`) não funcionava quando aplicada em itens vanilla (como espadas, picaretas e armaduras recém-fabricadas ou geradas sem modificações prévias). Os efeitos sonoros e visuais (raio, explosão) ocorriam e a mensagem `"Your item is now Unbreakable"` era exibida, mas o item permanecia quebrável e continuava perdendo durabilidade.

### 17.2 Causa Raiz
Em `UnbreakableRune.java`, o método `setUnbreakable` realizava a seguinte verificação:
```java
if (!isUnbreakable(item) && item.hasItemMeta()) {
    ItemMeta meta = item.getItemMeta();
    meta.setUnbreakable(true);
    item.setItemMeta(meta);
}
```
* **A pegadinha do `item.hasItemMeta()`:** No Bukkit/Spigot/Paper, um `ItemStack` vanilla comum (recém-criado, sem encantamentos, sem renomeação e com durabilidade cheia) retorna `hasItemMeta() == false`, pois ainda não possui metadados personalizados definidos.
* Como `hasItemMeta()` era `false`, o bloco `if` era completamente ignorado! O item era devolvido ao chão intacto sem receber a propriedade `setUnbreakable(true)`.
* Em contrapartida, itens do **Slimefun** já possuem nome, lore e chaves PDC personalizadas, de modo que para eles `hasItemMeta()` retornava `true`.
* Além disso, `findCompatibleItem` não filtrava se o item era de fato danificável (`getMaxDurability() > 0`), permitindo que a runa fosse acidentalmente consumida em itens que nem sequer possuem barra de vida (blocos, alimentos, sementes, etc.).

### 17.3 Solução Implementada
1. **Remoção da restrição `hasItemMeta()`:** Em Bukkit, `getItemMeta()` pode ser chamado diretamente para qualquer material que não seja ar, instanciando os metadados caso ainda não existam.
2. **Reparo e Visibilidade da Tag:** Quando o item se torna inquebrável, qualquer dano acumulado é restaurado (`((Damageable) meta).setDamage(0)`) e a flag `HIDE_UNBREAKABLE` é removida para que o cliente exiba a tag no tooltip.
3. **Filtro de Itens com Durabilidade:** Em `findCompatibleItem`, adicionada a verificação `itemStack.getType().getMaxDurability() > 0`, garantindo que apenas ferramentas, armas, armaduras e equipamentos reais sejam selecionados.
4. **Reobtenção segura da entidade:** No callback do agendador (`Scheduler.run(10, ...)`), o `item.getItemStack()` é lido novamente na hora da transformação, evitando desincronizações.
5. **Deploy:** `FoxyMachines` configurado com `slimefun-addon.gradle` atualizado, compilado na versão `v1.0` e `FoxyMachines-v1.0.jar` implantado no servidor.

---

## 18. Correção do Botão "Voltar" no Menu Cheat (`/sf cheat`) e Histórico de Navegação do Guia

### 18.1 O Problema
Ao utilizar o comando `/sf cheat` (guia do Slimefun no modo Cheat), ao entrar em uma categoria ou subcategoria (por exemplo, "Armas", "Energia", "Mochilas") e depois clicar no botão de **"Voltar"**, o menu não retornava para a categoria anterior ou página da categoria: ele pulava diretamente para a página principal (menu inicial com a lista de todos os addons/categorias principais).

### 18.2 Causa Raiz
A classe base da interface do guia é `SurvivalSlimefunGuide.java`, e a classe do modo cheat (`CheatSheetSlimefunGuide.java`) herda diretamente dela (`CheatSheetSlimefunGuide extends SurvivalSlimefunGuide`).
No código de `SurvivalSlimefunGuide.java`:
1. Nos métodos `openCategoryContents` (linha 499) e `openCategoryItemsFlat` (linha 556), o registro da categoria aberta no histórico de navegação do perfil do jogador (`profile.getGuideHistory().add(categoryGroup, page)`) estava protegido por uma verificação restritiva:
   ```java
   if (isSurvivalMode()) {
       profile.getGuideHistory().add(categoryGroup, page);
   }
   ```
   No modo Cheat, `getMode() == SlimefunGuideMode.CHEAT_MODE`, logo `isSurvivalMode()` retorna **`false`**. Portanto, nenhuma categoria ou página visitada no modo Cheat era registrada no histórico!
2. Quando o jogador clicava em "Voltar", o histórico possuía tamanho 1 (ou 0), caindo no bloco `else`:
   ```java
   } else {
       openMainMenu(profile, Slimefun.getRegistry().getAllItemGroups(), 1);
   }
   ```
   fazendo com que a interface saltasse direto para o menu inicial do guia.
3. Além disso, para subgrupos de itens (`SubItemGroup` e `NestedItemGroup`), caso o histórico estivesse vazio, o botão de voltar não possuía fallback para inspecionar `currentGroup.getParent()`.

### 18.3 Solução Implementada
No arquivo [`SurvivalSlimefunGuide.java`](file:///c:/Users/maiko/OneDrive/Documents/slimefun5%20addons/Slimefun5/core/src/main/java/io/github/thebusybiscuit/slimefun5/implementation/guide/SurvivalSlimefunGuide.java):
1. **Histórico Habilitado no Modo Cheat:**
   Removida a guarda `if (isSurvivalMode())` em `openCategoryContents` e `openCategoryItemsFlat`. Agora, `profile.getGuideHistory().add(categoryGroup, page)` é executado tanto no modo Survival quanto no modo Cheat.
2. **Navegação Hierárquica Aprimorada em `addBackButton`:**
   Adicionada sobrecarga `addBackButton(menu, slot, p, profile, @Nullable ItemGroup currentGroup)`.
   - Se `history.size() > 1`: retrocede pelo histórico (`history.goBack(this)`).
   - Se `history.size() <= 1`, mas o grupo atual for `SubItemGroup` com pai definido: abre `((SubItemGroup) currentGroup).getParent().open(...)`.
   - Se `history.size() <= 1`, mas o grupo atual for `NestedItemGroup` com pai definido: abre `((NestedItemGroup) currentGroup).getParent().open(...)`.
   - Caso contrário: volta para o menu principal (`openMainMenu`).
3. **Build e Deploy:**
   O Slimefun5 Core foi recompilado com sucesso (`./gradlew :core:build -x test`) gerando `Slimefun5-v1.0.jar` e implantado em `servidor_26.1.2/plugins/Slimefun5-v1.0.jar`.

---

## 19. Migração do Addon ElectricSpawners para Slimefun5 (v1.0)

### 19.1 O Que Mudou no ElectricSpawners
O addon **ElectricSpawners** foi atualizado e modernizado para compilar diretamente contra o fork **`com.github.MaikonPlotegher:Slimefun5:v1.0`** e **Paper API 1.21.x** (Java 21).

### 19.2 Principais Ajustes Realizados:
1. **Configuração Maven (`pom.xml`):**
   - Java Compiler atualizado para `21` (com `maven-compiler-plugin` 3.13.0).
   - Dependência do Slimefun atualizada para o fork:
     ```xml
     <dependency>
         <groupId>com.github.MaikonPlotegher</groupId>
         <artifactId>Slimefun5</artifactId>
         <version>v1.0</version>
         <scope>provided</scope>
     </dependency>
     ```
   - Paper API atualizada para `26.1.2.build.74-stable`.
   - Shade plugin atualizado para `3.5.2` com relocação do `bStats` 3.0.2.
   - Versão do artefato padronizada para `v1.0`.
2. **Atualização de Pacotes (`io.github.thebusybiscuit.slimefun4.*` -> `slimefun5.*`):**
   - Substituição de todos os imports legados pelo pacote do Slimefun5.
   - `NamespacedKey` migrado para `io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey`.
   - Texturas de cabeças migradas para `VersionedPlayerHead.getItemStack(...)`.
3. **Padrão de Criação de Itens no Slimefun5:**
   - Como `CustomItemStack` no Slimefun5 é uma fábrica utilitária estática, as chamadas `new CustomItemStack(...)` foram migradas para `CustomItemStack.create(...)`.
   - Para receitas contendo itens do Slimefun (`SlimefunItems.PLUTONIUM`, `ELECTRIC_MOTOR`, etc.), foi utilizado `.item()` para obter a instância nativa de `ItemStack`.
4. **Deploy:**
   - Compilado com sucesso gerando `ElectricSpawners-v1.0.jar` e copiado para a pasta de plugins do servidor (`servidor_26.1.2/plugins/ElectricSpawners-v1.0.jar`).

---

## 20. Migração do Addon MobCapturer para Slimefun5 (v1.0)

### 20.1 O Que Mudou no MobCapturer
O addon **MobCapturer** (canhão de captura de mobs e ovos de spawn gerados dinamicamente) foi completamente atualizado para compilar contra o fork **`com.github.MaikonPlotegher:Slimefun5:v1.0`** em ambiente moderno Paper 1.21.x e Java 25.

### 20.2 Principais Ajustes Realizados:
1. **Atualização do Gradle e Java:**
   - O Gradle Wrapper legado (8.5) causava incompatibilidade imediata com o JDK 25 (`java.lang.IllegalArgumentException: 25.0.3`). Foi atualizado para o **Gradle 9.3.1**.
   - `build.gradle.kts` configurado para `compileOnly("com.github.MaikonPlotegher:Slimefun5:v1.0")`, `compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")` e versão `v1.0`.
2. **Remoção de Dependências Externas Quebradas (`guizhanlib`):**
   - O plugin dependia de `net.guizhanss:guizhanlib-all:2.2.0-SNAPSHOT` via JitPack, que falhava no build com erro HTTP 401 Unauthorized.
   - A dependência foi completamente eliminada. Criou-se internamente as classes de utilidade [`MinecraftVersionUtil.java`](file:///c:/Users/maiko/OneDrive/Documents/slimefun5%20addons/em%20andamento/MobCapturer/src/main/java/io/github/thebusybiscuit/mobcapturer/utils/compatibility/MinecraftVersionUtil.java) e [`EntityTypeX.java`](file:///c:/Users/maiko/OneDrive/Documents/slimefun5%20addons/em%20andamento/MobCapturer/src/main/java/io/github/thebusybiscuit/mobcapturer/utils/compatibility/EntityTypeX.java) em `mobcapturer.utils.compatibility`, tornando o projeto 100% autônomo.
3. **Remoção do Lombok (Incompatibilidade com Java 25):**
   - O Lombok acessava estruturas internas do compilador `javac` (`com.sun.tools.javac.code.TypeTag::UNKNOWN`), gerando `ExceptionInInitializerError` no Java 25.
   - O Lombok foi removido de `build.gradle.kts` e as anotações `@UtilityClass` e `@Getter` foram substituídas por construtores privados e métodos getters nativos em todas as 14 classes utilitárias.
4. **Padronização para Slimefun5:**
   - Todos os imports migrados de `io.github.thebusybiscuit.slimefun4.*` para `slimefun5.*`.
   - `CustomItemStack` adaptado para o padrão estático `CustomItemStack.create(...)`.
   - Itens de receita (`MAGIC_LUMP_2`, `STEEL_INGOT`, `HOOK`, etc.) adaptados com `.item()`.
   - `NamespacedKey` migrado para a chave nativa do Slimefun5 (`io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey`).
5. **Deploy:**
   - Compilado com sucesso via Gradle gerando `MobCapturer-v1.0.jar` e copiado para a pasta de plugins do servidor (`servidor_26.1.2/plugins/MobCapturer-v1.0.jar`).

---

## 21. Migração do Addon SFCalc para Slimefun5 (v1.0)

### 21.1 O Que Mudou no SFCalc
O addon **SFCalc** (calculadora de receitas e materiais brutos do Slimefun) foi atualizado para compilar contra o fork **`com.github.MaikonPlotegher:Slimefun5:v1.0`** em Paper 1.21.x e Java 25.

### 21.2 Principais Ajustes Realizados:
1. **Configuração Gradle (`build.gradle.kts`):**
   - Atualizado para `compileOnly("com.github.MaikonPlotegher:Slimefun5:v1.0")` e `compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")`.
   - Compatibilidade de Java definida para Java 21+.
   - Nome do arquivo de saída configurado para `SFCalc-v1.0.jar`.
2. **Upgrade do Gradle Wrapper:**
   - O Gradle 8.3 original foi atualizado para o **Gradle 9.3.1** para suporte completo à JVM 25.
3. **Eliminação de Dependências Externas Frágeis:**
   - Dependência remota `ErrorReporter-Java` do JitPack foi substituída por uma classe local autônoma [`ErrorReporter.java`](file:///c:/Users/maiko/OneDrive/Documents/slimefun5%20addons/em%20andamento/SFCalc/src/main/java/io/github/seggan/errorreporter/ErrorReporter.java), evitando falhas de resolução e requisições externas em caso de erros.
   - Remoção completa do Lombok para evitar travamentos do `javac` no Java 25, utilizando métodos getters padrão.
4. **Migração para Slimefun5:**
   - Imports de receitas e instâncias migrados de `io.github.thebusybiscuit.slimefun4.*` para `slimefun5.*`.
   - `Validate` migrado para `io.github.thebusybiscuit.slimefun5.libraries.commons.lang.Validate` (eliminando a dependência do pacote antigo `org.apache.commons.lang.Validate` que não existe mais no Paper 1.21+).
5. **Modernização e Novas Funcionalidades Implementadas:**
   - **Interface Gráfica Interativa (`CalcMenu.java`):** Criado menu de 54 slots baseado em `ChestMenu` com paginação automática, exibindo os ícones reais de cada ingrediente, quantidade necessária, quantidade no inventário do jogador e o que falta coletar (com indicador verde/vermelho). O botão de filtro no slot 49 permite alternar entre "Ver Todos os Materiais" e "Ver Apenas Faltantes", e clicar em qualquer sub-ingrediente abre o cálculo dele recursivamente.
   - **Subcomando `/sfcalc need <item> [qtd]`:** Ativado o cálculo comparativo contra o inventário do jogador no chat.
   - **Subcomandos de Mão (`/sfcalc hand`, `/sfcalc handneed`, `/sfcalc handgui`):** Detectam automaticamente o item do Slimefun segurado na mão principal do jogador sem exigir digitação de IDs.
   - **Auto-complete Dinâmico:** Tab completer inteligente para todos os subcomandos, IDs de itens e quantidades rápidas (`1, 4, 16, 64`).
6. **Deploy:**
   - Compilado via Gradle gerando `SFCalc-v1.0.jar` e copiado para a pasta de plugins do servidor (`servidor_26.1.2/plugins/SFCalc-v1.0.jar`).





