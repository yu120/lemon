# lemon
The micro service gateway framework.

## Dubbo
The support apache dubbo 2.7.2 generic service proxy.

```
# URL
http://[host]:[port]/lemon/[application]/[service]/[method]?group=[group]&version=[version]

# JSON Body
List[
Map{},
Map{},
...
]
```

```
http://localhost:8080/lemon/micro-dubbo-provider/cn.micro.biz.dubbo.provider.DemoService/test

[{"name":"lemon", "age":23},{"10001"}]
```

## Motan

## HTTP