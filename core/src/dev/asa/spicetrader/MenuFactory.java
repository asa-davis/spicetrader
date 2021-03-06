package dev.asa.spicetrader;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;

public class MenuFactory {

	public static Menu createMenu(MenuManager manager, String menuType) {
		Vector2 pos;
		AtlasRegion backgroundTexture;
				
		if(menuType.equals("BoardedMenu")) {
			backgroundTexture = manager.getAtlas().findRegion("ui/game_over_menu_background");
			
			int topGap = 128;
			float x = (manager.getScreenSize().x / 2) - (backgroundTexture.getRegionWidth() / 2);
			float y = manager.getScreenSize().y - backgroundTexture.getRegionHeight() - topGap;
			pos = new Vector2(x, y);
			
			return new BoardedMenu(manager, pos, backgroundTexture);
		}
		
		else if(menuType.equals("DockedMenu")) {
			backgroundTexture = manager.getAtlas().findRegion("ui/game_over_menu_background");
			
			int topGap = 128;
			float x = (manager.getScreenSize().x / 2) - (backgroundTexture.getRegionWidth() / 2);
			float y = manager.getScreenSize().y - backgroundTexture.getRegionHeight() - topGap;
			pos = new Vector2(x, y);
			
			return new DockedMenu(manager, pos, backgroundTexture);
		}
		
		else if(menuType.equals("HUDMenu")) {
			backgroundTexture = manager.getAtlas().findRegion("ui/HUD_Base");
			pos = new Vector2(8,8);
			
			return new HUDMenu(manager, pos, backgroundTexture);
		}
		
		else if(menuType.equals("ShipMenu")) {
			backgroundTexture = manager.getAtlas().findRegion("ui/ship_menu_base");
			pos = new Vector2(8, 86);
			
			return new ShipMenu(manager, pos, backgroundTexture);
		}
		
		return null;
	}
	
}
