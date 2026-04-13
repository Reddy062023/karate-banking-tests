function fn() {
  var env = karate.env || 'local';
  karate.log('Environment:', env);

  var config = {
    env: env,
    baseUrl: 'http://localhost:8090',
    kafkaBootstrapServers: 'localhost:29092',
    schemaRegistryUrl: 'http://localhost:8081',
    mongoUri: 'mongodb://localhost:27017',
    mongoDatabase: 'bankingdb',
    wireMockUrl: 'http://localhost:8090'
  };

  if (env == 'staging') {
    karate.configure('ssl', true);
    config.baseUrl = 'http://staging-api:8090';
  }

  if (env == 'ci') {
    config.baseUrl = 'http://ci-api:8090';
  }

  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 10000);

  return config;
}