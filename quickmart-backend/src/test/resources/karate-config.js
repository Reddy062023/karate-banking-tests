function fn() {
  var env = karate.env || 'local';

  var config = {
    env: env,
    baseUrl: 'http://localhost:8085'
  };

  if (env == 'staging') {
    config.baseUrl = 'http://staging-api:8085';
  }

  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 10000);

  return config;
}