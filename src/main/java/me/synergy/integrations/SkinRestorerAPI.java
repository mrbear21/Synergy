package me.synergy.integrations;

import java.util.Optional;
import java.util.UUID;

import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.SkinProperty;

public class SkinRestorerAPI {

	public static String getSkinTextureURL(UUID uuid, String name) {
	    if (SkinsRestorerProvider.get() == null) {
	        return null;
	    }

	    if (uuid == null) {
	        return null;
	    }
	    
	    if (!SkinsRestorerProvider.get().getPlayerStorage().getSkinOfPlayer(uuid).isPresent()) {
	        return null;
	    }
	    
	    try {
	        Optional<SkinProperty> skin = SkinsRestorerProvider.get()
	                .getPlayerStorage()
	                .getSkinForPlayer(uuid, name);

	        return skin.map(SkinProperty::getValue)
	                   .map(MojangAPI::getSkinTextureURLStringFromBase64Blob)
	                   .orElse(null);

	    } catch (DataRequestException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

}
