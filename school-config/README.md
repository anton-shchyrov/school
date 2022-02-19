# school-config
Configuration service

Endpoints:
+ /config
  + Method: GET
  + Params: none
  + Returns: Full config. The config file must have the name `config.json` and be located near the jar file
    
+ /services/{name}
  + Method: GET
  + Params: none
  + Returns: Configuration for service `name`
