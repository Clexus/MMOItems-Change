package net.Indyuce.mmoitems.command.mmoitems.item;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.UpgradeStationGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 强化工作台命令节点
 * <p>
 * 命令格式：/mi item station [player]
 * </p>
 * <p>
 * 功能：为玩家打开强化工作台 GUI 界面
 * </p>
 * <p>
 * 参数说明：
 * <ul>
 *     <li>player - 可选，目标玩家名（需要 .others 权限）</li>
 * </ul>
 * </p>
 *
 * @author MMOItems Team
 * @since 强化系统扩展
 */
public class UpgradeStationCommandTreeNode extends CommandTreeNode {

    /**
     * 权限节点定义
     */
    private static final String PERM = "mmoitems.command.item.station";
    private static final String PERM_OTHERS = PERM + ".others";

    /**
     * 命令参数定义
     */
    private final Argument<Player> argPlayer;

    public UpgradeStationCommandTreeNode(CommandTreeNode parent) {
        super(parent, "station");

        // 可选的目标玩家参数
        argPlayer = addArgument(new Argument<>("player",
                (explorer, list) -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        list.add(p.getName());
                    }
                },
                (explorer, input) -> Bukkit.getPlayer(input),
                explorer -> null  // 默认为 null（为自己打开）
        ));
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        // 解析目标玩家参数
        Player target = explorer.parse(argPlayer);

        // 非玩家执行者（控制台）
        if (!(sender instanceof Player)) {
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "控制台使用时必须指定玩家: /mi item station <玩家>");
                return CommandResult.FAILURE;
            }
            new UpgradeStationGUI(target).open();
            sender.sendMessage(ChatColor.GREEN + "已为玩家 " + target.getName() + " 打开强化工作台。");
            return CommandResult.SUCCESS;
        }

        Player player = (Player) sender;

        // 基础权限检查
        if (!player.hasPermission(PERM)) {
            Message.NOT_ENOUGH_PERMS_COMMAND.format(ChatColor.RED).send(player);
            return CommandResult.FAILURE;
        }

        // 判断是为自己还是其他玩家打开
        if (target != null && !target.equals(player)) {
            // 为其他玩家打开需要额外权限
            if (!player.hasPermission(PERM_OTHERS)) {
                player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "你没有权限为其他玩家打开强化工作台。");
                return CommandResult.FAILURE;
            }
            new UpgradeStationGUI(target).open();
            player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "已为玩家 " + target.getName() + " 打开强化工作台。");
        } else {
            // 为自己打开
            new UpgradeStationGUI(player).open();
        }

        return CommandResult.SUCCESS;
    }
}
