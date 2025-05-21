# Admob Banner

Este projeto é um exemplo simples de integração do AdMob Banner em um aplicativo Android utilizando Jetpack Compose. O app faz uma requisição para o Ad Unit Id de teste do AdMob e exibe um banner de anúncio na parte inferior da tela.

## Tecnologias Utilizadas

- **Kotlin**
- **Jetpack Compose**
- **AdMob (Google Mobile Ads)**
- **Hilt (Injeção de dependência)**
- **Firebase Analytics & Crashlytics**

## Como rodar o projeto

1. **Clone o repositório:**
   ```sh
   git clone https://github.com/seu-usuario/admob-banner.git
   ```
2. **Abra o projeto no Android Studio.**
3. **Sincronize as dependências (Gradle Sync).**
4. **Crie o arquivo `local.properties`:**
   Crie um arquivo `local.properties` na pasta `app/` com as seguintes variáveis:
   ```local.properties
   # Add your AdMob keys locally (DO NOT commit to git):
   ADMOB_BANNER_AD_UNIT_ID_DEBUG=
   ADMOB_BANNER_AD_UNIT_ID_RELEASE=

   # Add your AdMob App IDs locally (DO NOT commit to git):
   ADMOB_APP_ID_DEBUG=
   ADMOB_APP_ID_RELEASE=
   ```
   > **Importante:**  
   > - Não comite este arquivo no controle de versão. Ele já está adicionado ao gitignore deste projeto
   > - Preencha os valores com suas próprias chaves do AdMob
   > - Para testes, você pode usar os IDs de teste do Google AdMob. [Veja aqui](https://developers.google.com/admob/android/quick-start)
5. **Execute o app em um dispositivo ou emulador Android.**

