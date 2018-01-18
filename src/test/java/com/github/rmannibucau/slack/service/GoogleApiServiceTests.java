package com.github.rmannibucau.slack.service;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;

import org.apache.meecrowave.junit.MonoMeecrowave;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(MonoMeecrowave.Runner.class)
public class GoogleApiServiceTests {

    @Inject
    private GooglePlaces googleApiService;

    @Ignore
    @Test
    public void getNearbyRestaurant() {

        final List<GooglePlaces.Restaurant> results = googleApiService.getNearbyRestaurant(null, 600, null);
        assertNotNull(results);
    }
}
