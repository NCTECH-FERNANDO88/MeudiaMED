# MeudiaMED

> Organização de estudos, plantões e finanças para profissionais e estudantes da saúde.

MeudiaMED reúne a rotina de quem concilia atendimento, preparação para residência e provas, além do controle de ganhos. É um aplicativo Android em desenvolvimento, com dados armazenados localmente no aparelho.

## O que já é possível fazer

- Registrar estudos de **Residência** e **Provas**, incluindo tempo de estudo e de descanso.
- Consultar o total de horas do mês e compará-lo ao mesmo período do mês anterior.
- Cadastrar ganhos de **Plantões** e valores **Fixos**.
- Identificar a forma de pagamento: Pix, transferência, cartão ou dinheiro.
- Filtrar os lançamentos financeiros e excluir registros quando necessário.
- Criar plantões com data e horário na agenda de trabalho.
- Manter e-mail, cargo, especialização e locais de trabalho nas configurações.
- Acompanhar uma tela inicial com resumo mensal, avisos e frase de organização diária.

## Público-alvo

Médicos, residentes, estudantes de medicina e demais profissionais da saúde que querem acompanhar prática clínica, formação e vida financeira em um só lugar.

## Tecnologias

| Área | Tecnologia |
| --- | --- |
| Linguagem | Kotlin |
| Interface | Jetpack Compose |
| Design system | Material 3 |
| Plataforma | Android |
| Build | Gradle |
| Persistência atual | SharedPreferences |
| IDE recomendada | Android Studio |

## Executando o projeto

1. Clone este repositório.
2. Abra a pasta do projeto no Android Studio.
3. Aguarde a sincronização do Gradle terminar.
4. Crie ou inicie um emulador Android.
5. Clique em **Run** para instalar o MeudiaMED.

Para usar o Terminal na raiz do projeto:

```bash
./gradlew assembleDebug
```

O APK de depuração será gerado em `app/build/outputs/apk/debug/`.

## Estrutura do projeto

```text
TaskFlowAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/taskflowandroid/
│   │   │   ├── MainActivity.kt
│   │   │   └── ui/theme/
│   │   └── res/
│   └── build.gradle.kts
├── docs/
│   └── GUIA_DE_TESTE.md
├── gradle/
├── README.md
├── CHANGELOG.md
└── LICENSE
```

## Documentação

- [Guia de teste manual](docs/GUIA_DE_TESTE.md)
- [Histórico de alterações](CHANGELOG.md)
- [Licença MIT](LICENSE)

## Próximos passos

- [ ] Salvar a agenda de plantões e os locais de trabalho permanentemente.
- [ ] Incluir metas mensais e cadastro de provas.
- [ ] Criar notificações e lembretes.
- [ ] Exportar relatórios mensais.
- [ ] Migrar do SharedPreferences para banco local com Room.
- [ ] Criar uma versão web.

## Licença

Distribuído sob a [licença MIT](LICENSE). Copyright © 2026 Nelson Costa.
