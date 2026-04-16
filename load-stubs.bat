@echo off
echo Loading WireMock stubs...
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\get-account-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\post-transfer-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\create-account-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\update-account-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\get-loan-eligibility-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\post-loan-apply-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\get-loan-status-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-missing-fields-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-404-account-not-found-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-422-insufficient-funds-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-same-account-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-invalid-currency-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-no-owner-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-transfer-missing-stub.json
echo All stubs loaded!