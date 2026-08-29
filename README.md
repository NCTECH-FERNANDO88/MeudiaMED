# MeudiaMED

Organização pessoal para profissionais e estudantes da saúde: estudos, plantões e finanças em um só lugar.

> Projeto Android em desenvolvimento inicial.

## Sobre

O MeudiaMED ajuda a conciliar a rotina intensa da área da saúde com estudos, plantões, finanças e descanso. O objetivo é concentrar informações essenciais em uma experiência simples, pensada para quem atende, estuda e precisa acompanhar a própria evolução.

## Problema

- Plantões e compromissos de trabalho mudam com frequência.
- A preparação para residência e provas exige consistência.
- Ganhos de plantões e renda fixa costumam ficar dispersos em planilhas e anotações.
- Ferramentas genéricas nem sempre refletem a rotina de profissionais da saúde.

## Funcionalidades atuais

- Registro de horas de estudo nas categorias **Residência** e **Provas**.
- Resumo mensal de estudos, com comparação ao mesmo período do mês anterior.
- Cadastro de ganhos de **Plantões** e renda **Fixa**.
- Resumo financeiro mensal com comparação ao mês anterior.
- Agenda para cadastrar data e horário de plantões.
- Tela inicial com avisos de plantões marcados para o dia seguinte e mensagem diária de organização.
- Configurações locais para e-mail, cargo e especialização.

## Público-alvo

Médicos, residentes, estudantes de medicina e outros profissionais da saúde que buscam organizar prática clínica, formação e finanças.

## Stack

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose
- **Design system:** Material 3
- **Plataforma:** Android
- **IDE:** Android Studio
- **Build:** Gradle
- **Armazenamento local atual:** SharedPreferences
- **Arquitetura atual:** aplicativo de tela única com componentes Compose

## Estrutura atual

```text
TaskFlowAndroid/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/taskflowandroid/
│       │   ├── MainActivity.kt
│       │   └── ui/theme/
│       │       ├── Color.kt
│       │       ├── Theme.kt
│       │       └── Type.kt
│       └── res/
│           ├── drawable/
│           ├── mipmap-*/
│           ├── values/
│           └── xml/
├── gradle/libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

## Roadmap

- [ ] Persistir agenda de plantões e locais de trabalho.
- [ ] Cadastro de provas e metas mensais de estudo.
- [ ] Notificações e lembretes.
- [ ] Exportação de relatórios mensais.
- [ ] Migração do SharedPreferences para banco local com Room.
- [ ] Suporte a múltiplos vínculos e fontes de renda.

## Licença

Distribuído sob a licença MIT. Consulte [LICENSE](LICENSE) para mais detalhes.
