# Shadow Player

An audio player for shadow reading
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/superlidengke/shadowPlayer/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/superlidengke/shadowPlayer/tree/master)

### Build and Run docker local

```shell
## in project root dir
docker build -t showplayer -f .circleci/Dockerfile .
docker run -it --entrypoint sh showplayer 
```

## Issues

1. When updated to API Level33 revision 2, the file dragged to the
   /storage/emulated/0/Documents/
   by DeviceExplorer can't be read by code. Though the file owner and permission
   seemed no problem,
   it will report permission deny. The file copied by test code is OK.