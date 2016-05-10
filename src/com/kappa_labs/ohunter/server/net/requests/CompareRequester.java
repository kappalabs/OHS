package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.CompareRequest;
import com.kappa_labs.ohunter.server.analyzer.Analyzer;

/**
 * Implementation of the CompareRequest from the OHL.
 */
public class CompareRequester extends CompareRequest {

    public CompareRequester(CompareRequest request) {
        super(request);
    }

    @Override
    public Response execute() throws OHException {
        float bestSimilarity = 0;
        for (Photo similarPhoto : similarPhotos) {
            float similarity = Analyzer.computeSimilarity(referencePhoto, similarPhoto);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
            }
        }
        Response response = new Response(uid);
        response.similarity = bestSimilarity;
        if (referencePhoto.daytime == Photo.DAYTIME.NIGHT) {
            response.similarity /= 2;
        }

        return response;
    }

}
