#!/usr/bin/env bash
# Trava do CI: todo use case do core precisa ter um teste de unidade com Mockito.
# Para cada  src/main/java/.../FooUseCaseImpl.java
# exige      src/test/java/.../FooUseCaseImplTest.java  (mesmo pacote) e que ele use org.mockito.
# Rodar na mão: bash scripts/check-unit-tests.sh
set -euo pipefail

missing=0

while IFS= read -r impl; do
  test_file="${impl/src\/main\/java/src/test/java}"
  test_file="${test_file%.java}Test.java"

  if [[ ! -f "$test_file" ]]; then
    echo "❌ Sem teste: $impl"
    echo "   crie:      $test_file"
    missing=1
  elif ! grep -q "org\.mockito" "$test_file"; then
    echo "❌ Teste sem Mockito: $test_file"
    echo "   o teste de unidade precisa mockar o gateway com @Mock / @ExtendWith(MockitoExtension.class)"
    missing=1
  else
    echo "✅ $test_file"
  fi
done < <(find src/main/java/com/enveloppe/core/useCases -name '*UseCaseImpl.java' | sort)

if [[ $missing -ne 0 ]]; then
  echo
  echo "Todo use case precisa de teste de unidade com Mockito antes de subir."
  exit 1
fi

echo "Todos os use cases têm teste de unidade com Mockito"
