/*
 * The MIT License
 *
 *  Copyright (c) 2015, CloudBees, Inc.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 */

package com.amazonaws.services.ecs;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.AmazonWebServiceResponse;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.handlers.HandlerChainFactory;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.http.JsonErrorResponseHandler;
import com.amazonaws.http.JsonResponseHandler;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.ecs.model.DeleteClusterRequest;
import com.amazonaws.services.ecs.model.DeleteClusterResult;
import com.amazonaws.services.ecs.model.transform.ClientExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.ClusterContainsContainerInstancesExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.ClusterContainsServicesExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.ClusterNotFoundExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.DeleteClusterRequestMarshaller;
import com.amazonaws.services.ecs.model.transform.DeleteClusterResultJsonUnmarshaller;
import com.amazonaws.services.ecs.model.transform.InvalidParameterExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.MissingVersionExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.NoUpdateAvailableExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.ServerExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.ServiceNotActiveExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.ServiceNotFoundExceptionUnmarshaller;
import com.amazonaws.services.ecs.model.transform.UpdateInProgressExceptionUnmarshaller;
import com.amazonaws.transform.JsonErrorUnmarshaller;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.amazonaws.util.AWSRequestMetrics;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class AmazonECRClient extends AmazonWebServiceClient implements AmazonECR {

    /** Provider for AWS credentials. */
    private AWSCredentialsProvider awsCredentialsProvider;

    private static final Log log = LogFactory.getLog(AmazonECS.class);

    /**
     * List of exception unmarshallers for all AmazonECS exceptions.
     */
    protected List<JsonErrorUnmarshaller> jsonErrorUnmarshallers;

    public AmazonECRClient(AWSCredentials awsCredentials, ClientConfiguration clientConfiguration) {
        super(clientConfiguration);

        this.awsCredentialsProvider = new StaticCredentialsProvider(awsCredentials);

        init();
    }

    private void init() {
        jsonErrorUnmarshallers = new ArrayList<JsonErrorUnmarshaller>();
        jsonErrorUnmarshallers.add(new ClusterContainsContainerInstancesExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new ServerExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new MissingVersionExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new ClusterContainsServicesExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new ServiceNotActiveExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new ClientExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new UpdateInProgressExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new ClusterNotFoundExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new InvalidParameterExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new NoUpdateAvailableExceptionUnmarshaller());
        jsonErrorUnmarshallers.add(new ServiceNotFoundExceptionUnmarshaller());

        jsonErrorUnmarshallers.add(new JsonErrorUnmarshaller());

        // calling this.setEndPoint(...) will also modify the signer accordingly
        this.setEndpoint("ecr.us-east-1.amazonaws.com");

        HandlerChainFactory chainFactory = new HandlerChainFactory();
        requestHandler2s.addAll(chainFactory.newRequestHandlerChain(
                "/com/amazonaws/services/ecr/request.handlers"));
        requestHandler2s.addAll(chainFactory.newRequestHandler2Chain(
                "/com/amazonaws/services/ecr/request.handler2s"));
    }

    @Override
    public GetAuthorizationTokenResult getAuthorizationToken(GetAuthorizationTokenRequest getAuthorizationTokenRequest) throws AmazonServiceException, AmazonClientException {
        ExecutionContext executionContext = createExecutionContext(getAuthorizationTokenRequest);
        Request<GetAuthorizationTokenRequest> request = null;
        Response<GetAuthorizationTokenResult> response = null;

        request = new GetAuthorizationTokenRequestMarshaller().marshall(super.beforeMarshalling(getAuthorizationTokenRequest));

        Unmarshaller<GetAuthorizationTokenResult, JsonUnmarshallerContext> unmarshaller =
                new GetAuthorizationTokenResultJsonUnmarshaller();
        JsonResponseHandler<GetAuthorizationTokenResult> responseHandler =
                new JsonResponseHandler<GetAuthorizationTokenResult>(unmarshaller);

        response = invoke(request, responseHandler, executionContext);

        return response.getAwsResponse();
    }

    private <X, Y extends AmazonWebServiceRequest> Response<X> invoke(Request<Y> request,
                                                                      HttpResponseHandler<AmazonWebServiceResponse<X>> responseHandler,
                                                                      ExecutionContext executionContext) {
        request.setEndpoint(endpoint);
        request.setTimeOffset(timeOffset);

        AWSCredentials credentials = awsCredentialsProvider.getCredentials();

        AmazonWebServiceRequest originalRequest = request.getOriginalRequest();
        if (originalRequest != null && originalRequest.getRequestCredentials() != null) {
            credentials = originalRequest.getRequestCredentials();
        }

        executionContext.setCredentials(credentials);
        JsonErrorResponseHandler errorResponseHandler = new JsonErrorResponseHandler(jsonErrorUnmarshallers);
        Response<X> result = client.execute(request, responseHandler, errorResponseHandler, executionContext);
        return result;
    }
}
