FROM redis:7

ENV SENTINEL_MASTER_NAME=kelarus-ha
ENV SENTINEL_MASTER_HOST=kelarus-platform-redis-ha-master
ENV SENTINEL_MASTER_PORT=6379

ENV SENTINEL_QUORUM=2
ENV SENTINEL_DOWN_AFTER=1000
ENV SENTINEL_FAILOVER=15000

RUN mkdir -p /redis

WORKDIR /redis

COPY redis/sentinel.conf /redis/sentinel.conf
COPY redis/sentinel-entrypoint.sh /usr/local/bin/sentinel-entrypoint.sh

RUN chown -R redis:redis /redis \
    && chmod 755 /usr/local/bin/sentinel-entrypoint.sh

EXPOSE 26379

ENTRYPOINT ["sentinel-entrypoint.sh"]