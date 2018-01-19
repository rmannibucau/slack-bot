package com.github.rmannibucau.slack.service;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.apache.meecrowave.junit.MonoMeecrowave;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(MonoMeecrowave.Runner.class)
public class JoiesDuCodeServiceTest {

    @Inject
    private JoiesDuCodeService service;

    @Ignore
    @Test
    public void randomTest() {
        final JoiesDuCodeService.Gif gif = service.random();
        assertNotNull(gif);
    }
}
