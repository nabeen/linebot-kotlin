# linebot kotlin

This application is a LINE BOT written in Kotlin running on AWS Lambda.

And based on [moritalous/linebot\-serverless\-blueprint\-java](https://github.com/moritalous/linebot-serverless-blueprint-java).

## Usage

### Create config files

```bash
# for development
$ touch ./config/dev/environment.yml
# for production
$ touch ./config/prod/environment.yml
```

### Set your keys in above files

```yml
CHANNEL_SECRET: FOO
CHANNEL_ACCESS_TOKEN: BAR
```

### Deploy on your AWS!!

```bash
$ npm run deploy
```

## Reference

* [moritalous/linebot\-serverless\-blueprint\-java](https://github.com/moritalous/linebot-serverless-blueprint-java)
* [linebot\-serverless\-blueprint\-javaを作った！](https://qiita.com/moritalous/items/af4f05543a1b8817e472)
