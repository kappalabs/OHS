
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.entities.Photo;
import com.kappa_labs.ohunter.entities.Player;
import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;
import com.kappa_labs.ohunter.server.analyzer.Analyzer;


public class CompareRequest extends com.kappa_labs.ohunter.requests.CompareRequest {

    public CompareRequest(Player player, Photo ph1, Photo ph2) {
        super(player, ph1, ph2);
    }
    
    @Override
    public Response execute() throws OHException {
        float similarity = Analyzer.computeSimilarity(ph1, ph2);
        Response response = new Response(uid);
        response.similarity = similarity;
        
        return response;
    }

}
