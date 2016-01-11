
package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.Request;
import com.kappa_labs.ohunter.server.analyzer.Analyzer;


public class CompareRequest extends com.kappa_labs.ohunter.lib.requests.CompareRequest {

    public CompareRequest(Player player, Photo ph1, Photo ph2) {
        super(player, ph1, ph2);
    }
    
    public CompareRequest(Request r) {
        super((com.kappa_labs.ohunter.lib.requests.CompareRequest) r);
    }
    
    @Override
    public Response execute() throws OHException {
        float similarity = Analyzer.computeSimilarity(ph1, ph2);
        Response response = new Response(uid);
        response.similarity = similarity;
        
        return response;
    }

}
