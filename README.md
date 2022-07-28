# Run Infinispan:
```shell
docker run -it -p 11222:11222 -e USER="admin" -e PASS="admin" --network host quay.io/infinispan/server:13.0
```

Run `BloomFilter#main`