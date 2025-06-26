-- 删除SSE演示的按钮权限
DELETE FROM sys_menu WHERE menu_name LIKE 'SSE演示%';

-- 删除演示管理菜单（如果没有其他子菜单）
DELETE FROM sys_menu WHERE menu_name = '演示管理' AND NOT EXISTS (
    SELECT 1 FROM sys_menu sm WHERE sm.parent_id = sys_menu.menu_id
); 