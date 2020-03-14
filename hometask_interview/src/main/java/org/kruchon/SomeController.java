package org.kruchon;

import org.kruchon.annotations.DbLock;
import org.kruchon.annotations.SynchronizedInMemoryLock;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SomeController {

    @SynchronizedInMemoryLock
    @PostMapping("a")
    public void doA(@RequestParam("id") String id) throws InterruptedException {
        Thread.sleep(1000L);
    }

    @SynchronizedInMemoryLock
    @PostMapping("b")
    public void doB(@RequestParam("id") String id) throws InterruptedException {
        Thread.sleep(1000L);
    }

    @PostMapping("c")
    public void doC(@RequestParam("id") String id){

    }
}
