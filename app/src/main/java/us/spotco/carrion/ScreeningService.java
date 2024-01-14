/*
Copyright (c) 2024 Divested Computing Group

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package us.spotco.carrion;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.util.Log;

public class ScreeningService extends CallScreeningService {
    @Override
    public void onScreenCall(Call.Details details) {
        boolean isIncoming = details.getCallDirection() == Call.Details.DIRECTION_INCOMING;
        Log.d("Carrion", "Received call");
        if (isIncoming && !isEmergencyCall(details)) {
            Log.d("Carrion", "Verification " + details.getCallerNumberVerificationStatus());
            switch (details.getCallerNumberVerificationStatus()) {
                case Connection.VERIFICATION_STATUS_FAILED:
                    callReject(details);
                    break;
                case Connection.VERIFICATION_STATUS_PASSED:
                    callAllow(details);
                    break;
                default:
                    // Network could not perform verification.
                    // This branch matches Connection.VERIFICATION_STATUS_NOT_VERIFIED
                    //Add control of this case;
                    callAllow(details);
                    break;
            }
        } else {
            callAllow(details);
        }
    }

    public boolean isEmergencyCall(Call.Details details) {
        return details.hasProperty(Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE)
                || details.hasProperty(Call.Details.PROPERTY_NETWORK_IDENTIFIED_EMERGENCY_CALL);
    }

    public void callReject(Call.Details details) {
        Log.d("Carrion", "Rejecting call");
        CallResponse.Builder response = new CallResponse.Builder();
        response.setDisallowCall(true);
        response.setRejectCall(true);
        respondToCall(details, response.build());
    }

    public void callAllow(Call.Details details) {
        Log.d("Carrion", "Allowing call");
        respondToCall(details, new CallResponse.Builder().build());
    }
}
