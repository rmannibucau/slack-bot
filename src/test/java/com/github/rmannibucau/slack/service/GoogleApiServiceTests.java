package com.github.rmannibucau.slack.service;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.apache.meecrowave.junit.MonoMeecrowave;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore("don't call google by default")
@RunWith(MonoMeecrowave.Runner.class)
public class GoogleApiServiceTests {

    @Inject
    private GooglePlaces googleApiService;


    @Test
    public void getNearbyRestaurant() {

        final GooglePlaces.Result results = googleApiService.getNearbyRestaurant(null, 600, "sushi");
        assertNotNull(results);
    }
}
