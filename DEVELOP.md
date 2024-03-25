## Development Requirements

### fundamental mode

1、The Struts for the data：~~json、form、string(all)~~、xml -> use wrapper class to parse the data(body+header+url)

2、Encrypted Sign：~~all body、Single sign(based on headers)、single sign(based on body)~~

3、Request and Response package：~~entire request body crypto、some params cryptoed in request body~~、entire response body、some params crypro in response body

4、Crypto class：~~base64、aes、md5~~、sekiro

5、The encryption client: web、miniprogram、app

### problem

1、Only crytoed for the exist param, not for the unexist param

2、Does not have some test data for the respone mode、the mitmproxy logic in response mode

3、The sekiro could not start in a normal server


## Codeing Example

```
POST /SSRF/URLConnection/vul HTTP/1.1
Host: 10.211.55.223:8888
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
Referer: http://10.211.55.223:8888/index/ssrf
Accept-Encoding: gzip, deflate
Accept-Language: en,zh-CN;q=0.9,zh;q=0.8
Cookie: JSESSIONID=C85ACEFD970EF02EBA84A4445C55723C; JWT_TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MTAxNzExNTUsImV4cCI6MTcxMDI1NzU1NSwidXNlcm5hbWUiOiJhZG1pbiJ9.WhEXrgB-YLtpMOFdT3cjfZtfFgdEn8Y1iot8k7vuF4M
Connection: close
Content-Type: application/text
Content-Length: 76

{"abc":"bbbb","aaa":[1,2,{"vvv":"adadasd"}],"qqq":{"www":"www","ccc":"lll"}}




POST /SSRF/URLConnection/vul?aa=xxx&bb=asd HTTP/1.1
Host: 10.211.55.223:8888
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
Referer: http://10.211.55.223:8888/index/ssrf
Accept-Encoding: gzip, deflate
Accept-Language: en,zh-CN;q=0.9,zh;q=0.8
Cookie: JSESSIONID=C85ACEFD970EF02EBA84A4445C55723C; JWT_TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MTAxNzExNTUsImV4cCI6MTcxMDI1NzU1NSwidXNlcm5hbWUiOiJhZG1pbiJ9.WhEXrgB-YLtpMOFdT3cjfZtfFgdEn8Y1iot8k7vuF4M
Connection: close
Content-Type: application/text
Content-Length: 49

xx=xx&a=xx&adc=MTIzasdasdasdaNA==&111=1234555

```