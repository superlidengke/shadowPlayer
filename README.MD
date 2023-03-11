# Shadow Player

An audio player for shadow reading
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/superlidengke/shadowPlayer/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/superlidengke/shadowPlayer/tree/master)

### Build and Run docker local

```shell
## in project root dir
docker build -t showplayer -f .circleci/Dockerfile .
docker run -it --entrypoint sh showplayer 
```