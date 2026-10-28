# UpCar

O **UpCar** é um aplicativo Android desenvolvido em Kotlin que simula a experiência de solicitar e acompanhar uma carona até um campus universitário. O projeto permite explorar as principais etapas do processo, desde o cadastro do passageiro e a escolha do motorista até a confirmação, o acompanhamento da corrida e a avaliação final.

## Funcionalidades

- **Cadastro de passageiro:** registro de informações pessoais e bairro de origem.
- **Lista de motoristas:** visualização de opções com informações como veículo, placa, horário, preço e avaliação.
- **Solicitação de carona:** criação de uma solicitação vinculada ao motorista escolhido.
- **Chat e negociação:** envio de mensagens, sugestão de contraproposta e confirmação do valor.
- **Mapa:** visualização de motoristas e acompanhamento da corrida com recursos de mapa.
- **Acompanhamento da corrida:** progresso simulado até a finalização.
- **Perfil do motorista:** consulta das informações e do histórico de corridas.
- **Avaliação:** atribuição de nota, comentário e elogios após a corrida.
- **Persistência local:** armazenamento de passageiros, motoristas, caronas, mensagens e avaliações no banco de dados local.

> **Observação:** o fluxo de negociação e o deslocamento são simulados. O projeto não deve ser entendido como um serviço de transporte conectado a motoristas reais ou como um sistema de localização em tempo real.

## Como funciona

O fluxo principal do aplicativo é o seguinte:

1. **Boas-vindas:** o usuário acessa a tela inicial e inicia o processo.
2. **Cadastro:** informa seus dados para utilizar o aplicativo.
3. **Escolha do motorista:** consulta os motoristas disponíveis e seleciona uma opção de carona.
4. **Solicitação e chat:** o aplicativo cria uma carona e abre uma conversa associada a ela. O passageiro pode enviar mensagens e sugerir um valor.
5. **Confirmação:** após a negociação simulada, o passageiro confirma a carona e segue para a tela de acompanhamento.
6. **Corrida em andamento:** o progresso é atualizado por uma simulação de duração aproximada de um minuto. Também é possível consultar o perfil do motorista.
7. **Finalização e avaliação:** ao concluir a corrida, o passageiro pode avaliar a experiência. A avaliação atualiza a nota média e o histórico do motorista.

## Tecnologias utilizadas

- **Kotlin:** linguagem principal do aplicativo.
- **Android SDK:** plataforma de execução.
- **Jetpack Compose e Material 3:** construção das interfaces.
- **Navigation Compose:** navegação entre as telas.
- **ViewModel e Coroutines:** gerenciamento de estado e execução de operações assíncronas.
- **Room:** persistência de dados em banco local SQLite.
- **OSMDroid:** recursos de mapas.
- **Gradle Kotlin DSL:** configuração e gerenciamento da compilação.

## Estrutura do projeto

Os principais arquivos estão em `app/src/main/java/com/example/upcar/`:

| Arquivo | Responsabilidade |
| --- | --- |
| `MainActivity.kt` | Ponto de entrada da interface Android. |
| `AppNavigation.kt` | Define as rotas e o fluxo de navegação. |
| `BoasVindas.kt` | Tela inicial. |
| `CadastroPassageiro.kt` | Cadastro do passageiro. |
| `ProcuraMotorista.kt` | Listagem e seleção de motoristas. |
| `PerfilMotorista.kt` | Exibição do perfil do motorista. |
| `Chat.kt` | Mensagens, contraproposta e confirmação da carona. |
| `CorridaEmAndamento.kt` | Acompanhamento da corrida. |
| `CorridaFinalizada.kt` | Resumo e avaliação da corrida. |
| `MapaMotoristas.kt` e `MapaCorrida.kt` | Componentes de mapa. |
| `CaronaViewModel.kt` | Regras de negócio, estado e coordenação dos dados. |
| `Modelos.kt` | Entidades e modelos de dados. |
| `AppDatabase.kt` | Banco Room, DAOs e conversores. |
| `Validacoes.kt` e `Formatadores.kt` | Funções auxiliares de validação e formatação. |

## Como executar

### Pré-requisitos

- Android Studio compatível com a configuração do projeto.
- JDK compatível com a versão do Android Gradle Plugin utilizada.
- Android SDK e ferramentas de compilação exigidas pelo projeto.
- Emulador Android ou dispositivo físico para executar o aplicativo.

### Passos

1. Clone o repositório:

   ```bash
   git clone https://github.com/LuanNeuwirthC/upcar3.git
   ```

2. Entre na pasta do projeto:

   ```bash
   cd upcar3
   ```

3. Abra a pasta no Android Studio e aguarde a sincronização do Gradle.
4. Se necessário, instale as versões do Android SDK solicitadas pelo projeto.
5. Selecione um emulador ou dispositivo Android e execute a configuração `app`.

No Windows, também é possível compilar pelo terminal usando o Gradle Wrapper:

```powershell
.\gradlew.bat assembleDebug
```

No Linux ou macOS:

```bash
./gradlew assembleDebug
```

## Armazenamento de dados

O aplicativo utiliza o Room para manter dados localmente no dispositivo. Entre as entidades persistidas estão:

- Passageiros;
- Motoristas;
- Caronas;
- Mensagens;
- Avaliações.

Os motoristas iniciais são carregados pelo aplicativo quando o banco ainda não possui registros. A persistência é local: o repositório não apresenta, no fluxo analisado, um backend remoto para sincronizar dados entre diferentes dispositivos.

## Limitações atuais

- A negociação com o motorista é simulada pelo próprio aplicativo.
- O deslocamento e o progresso da corrida são simulados.
- Os dados são armazenados localmente, sem sincronização por servidor identificada no código analisado.
- A disponibilidade de mapas depende da configuração de rede e dos serviços de mapas utilizados.

## Objetivo do projeto

O UpCar demonstra a implementação de um fluxo de caronas em um aplicativo Android, reunindo navegação entre telas, gerenciamento de estado, persistência local, mapas e interação simulada com o usuário.

---

Desenvolvido como projeto acadêmico.
