@RestController
class Demo {
    @RequestMapping("/hello/{name}")
    def hello(@PathVariable("name") String name) {
        return 'Hello ' + name
    }
}
