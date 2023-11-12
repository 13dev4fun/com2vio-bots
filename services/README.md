# Com2Vio Services

Components:
* app - a spring-boot application
* db - a mysql database
* model - a pytorch model

# How to run it
1. run `docker-compose -f ./docker-compose.yml up -d`, this command setup kafka, database and pytorch model
2. cd `./app`, update `src/main/resources/application.yml` with your github token, run `mvn spring-boot:run`
3. when spring-boot application is started, the service is listening on port 9086 by default
4. entry points:

   * Add a repository and start fetching pull requests and comments, commits and files information of this repo
   ```
   curl --location 'http://localhost:9086/repo' \
        --header 'Content-Type: application/json' \
        --data '{
            "owner": "google",
            "repo": "gson",
            "base": "master"
        }'
   ```
   * Once pull requests related inform ation fetched, find commits that exists in a PR commit list (sometimes due to force-push, commit that contains a comment might not exist in a PR's final commit list, when we download files based on commit, this files are not found. So we do not take this kind of comments into consideration, we need to filter out such comments before downloading files)
   ```
   curl --location 'http://localhost:9086/commitComment' \
        --header 'Content-Type: application/json' \
        --data '{
            "owner": "google",
            "repo": "gson"
        }'
   ``` 
   * Then start to download files
   ```
   curl --location 'http://localhost:9086/download' \
        --header 'Content-Type: application/json' \
        --data '{
            "owner": "google",
            "repo": "gson"
        }'
   ```

   * **TODO** - Run SonarQube and Violation tracker to get violations

   * Match violations and review comments, when matching is down, the app will send a label-event to the model
   ```
   curl --location 'http://localhost:9086/match' \
        --header 'Content-Type: application/json' \
        --data '{
            "owner": "google",
            "repo": "gson"
        }'
   ```

   * After matches are labeled, trigger post-processing for further analyzing
   ```
   curl --location 'http://localhost:9086/post-processing' \
        --header 'Content-Type: application/json' \
        --data '{
            "owner": "google",
            "repo": "gson"
        }'
   ```

   * **TODO** Recommending rules
   ```
   curl --location 'http://localhost:9086/rules?owner=google&repo=gson' \
        --header 'Content-Type: application/json'
   ```