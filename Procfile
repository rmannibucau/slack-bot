web: cd target && unzip slack-bot-meecrowave-distribution.zip && cd slack-bot-distribution && MEECROWAVE_OPTS="-Dtalend.slack.bot.channels=$SLACK_CHANNELS -Dtalend.slack.bot.slack.api.token=$SLACK_TOKEN -Dtalend.slack.bot.weatherbit.api.key=$WEATHERBIT_TOKEN -Dtalend.slack.bot.google.api.key=$GOOGLE_TOKEN" ./bin/meecrowave.sh run --http=$PORT
