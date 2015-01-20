#!/bin/bash
cp target/jnlp/lib/app-signer-client-linux-x86_64.jar ../app-signer-web/src/main/webapp/lib/app-signer-client-linux-x86_64.jar
cp target/jnlp/lib/app-signer-client-linux-x86.jar ../app-signer-web/src/main/webapp/lib/app-signer-client-linux-x86.jar
cp target/jnlp/lib/app-signer-client-windows-x86_64.jar ../app-signer-web/src/main/webapp/lib/app-signer-client-windows-x86_64.jar
cp target/jnlp/lib/app-signer-client-windows-x86.jar ../app-signer-web/src/main/webapp/lib/app-signer-client-windows-x86.jar
echo "Copiados los jars al webapp/lib para generar el war"
