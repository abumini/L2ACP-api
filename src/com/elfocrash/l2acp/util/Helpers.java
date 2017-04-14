package com.elfocrash.l2acp.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.elfocrash.l2acp.models.BuyListItem;
import com.elfocrash.l2acp.models.DonateService;
import com.elfocrash.l2acp.models.PlayerInfo;
import com.elfocrash.l2acp.responses.L2ACPResponse;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Helpers {
	public static int getPlayerIdByName(String name){
		String query = "SELECT obj_Id FROM characters WHERE char_name=?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, name);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					int objId = rset.getInt("obj_Id");
					return objId;
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void removeDonatePoints(String accountName, int price){
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("update accounts set donatepoints=(donatepoints - ?) WHERE login=?"))
		{
			ps.setInt(1, price);
			ps.setString(2, accountName);
			ps.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static int getDonatePoints(String accountName){
		String query = "SELECT donatepoints FROM accounts WHERE login=?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, accountName);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					int donatepoints = rset.getInt("donatepoints");
					return donatepoints;
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public static String getAccountName(String charName){
		String query = "SELECT account_name FROM characters WHERE char_name=?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, charName);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					String accName = rset.getString("account_name");
					return accName;
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return charName;
	}
	
	public static ArrayList<BuyListItem> getDonateItemList(){
		
		ArrayList<BuyListItem> invInfo = new ArrayList<BuyListItem>(); 
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT itemId,itemCount,enchant,price FROM donateitems");
			ResultSet itemList = statement.executeQuery();
			
			while (itemList.next())// fills the package
			{
				int itemId = itemList.getInt("itemId");
				int itemCount = itemList.getInt("itemCount");
				int enchant = itemList.getInt("enchant");
				int price = itemList.getInt("price");
				invInfo.add(new BuyListItem(itemId,itemCount,enchant,price));
			}
			
			itemList.close();
			statement.close();			
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return invInfo;
	}
	
	public static ArrayList<String> getAllPlayerNames(){
		
		ArrayList<String> names = new ArrayList<String>(); 
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters");
			ResultSet nameList = statement.executeQuery();
			
			while (nameList.next())// fills the package
			{
				String name = nameList.getString("char_name");
				names.add(name);
			}
			
			nameList.close();
			statement.close();			
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return names;
	}
	
	public static void banChatOfflinePlayer(String name, int delay, boolean ban)
	{
		int level = 0;
		long value = 0;
		
		if (ban)
		{
			level = L2PcInstance.PunishLevel.CHAT.value();
			value = (delay > 0 ? delay * 60000L : 60000);
		}
		else
		{
			level = L2PcInstance.PunishLevel.NONE.value();
			value = 0;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET punish_level=?, punish_timer=? WHERE char_name=?");
			statement.setInt(1, level);
			statement.setLong(2, value);
			statement.setString(3, name);
			
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
		}
		catch (SQLException se)
		{
		}
	}
	
	public static void jailOfflinePlayer(String name, int delay)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE char_name=?");
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, L2PcInstance.PunishLevel.JAIL.value());
			statement.setLong(5, (delay > 0 ? delay * 60000L : 0));
			statement.setString(6, name);
			
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
			
		}
		catch (SQLException se)
		{
		}
	}
	
	public static void unjailOfflinePlayer(String name)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE char_name=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
		}
		catch (SQLException se)
		{
		}
	}
	
	public static boolean changeCharAccessLevel(L2PcInstance targetPlayer, String player, int lvl)
	{
		if (targetPlayer != null)
		{
			targetPlayer.setAccessLevel(lvl);
			targetPlayer.logout();
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?");
				statement.setInt(1, lvl);
				statement.setString(2, player);
				statement.execute();
				int count = statement.getUpdateCount();
				statement.close();
				
				if (count == 0)
				{
					return false;
				}
			}
			catch (SQLException se)
			{				
				return false;
			}
		}
		return true;
	}
	
	public static ArrayList<DonateService> getDonateServices(){
			
			ArrayList<DonateService> invInfo = new ArrayList<DonateService>(); 
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT serviceid,servicename,price FROM donateservices");
				ResultSet itemList = statement.executeQuery();
				
				while (itemList.next())// fills the package
				{
					int serviceId = itemList.getInt("serviceid");
					String serviceName = itemList.getString("servicename");
					int price = itemList.getInt("price");
					invInfo.add(new DonateService(serviceId,serviceName,price));
				}
				
				itemList.close();
				statement.close();			
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return invInfo;
		}
	}
