-- 创建演示管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('演示管理', 0, 10, 'demo', null, 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate(), '', null, '演示管理目录');

-- 获取演示管理菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 创建SSE演示菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('SSE演示', @parentId, 1, 'sse', 'demo/sse/index', 1, 0, 'C', '0', '0', 'demo:sse:list', 'message', 'admin', sysdate(), '', null, 'SSE演示菜单');

-- 获取SSE演示菜单ID
SELECT @menuId := LAST_INSERT_ID();

-- 创建SSE演示按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES 
('SSE演示查询', @menuId, 1, '#', '', 1, 0, 'F', '0', '0', 'demo:sse:query', '#', 'admin', sysdate(), '', null, ''),
('SSE演示新增', @menuId, 2, '#', '', 1, 0, 'F', '0', '0', 'demo:sse:add', '#', 'admin', sysdate(), '', null, ''),
('SSE演示修改', @menuId, 3, '#', '', 1, 0, 'F', '0', '0', 'demo:sse:edit', '#', 'admin', sysdate(), '', null, ''),
('SSE演示删除', @menuId, 4, '#', '', 1, 0, 'F', '0', '0', 'demo:sse:remove', '#', 'admin', sysdate(), '', null, ''),
('SSE演示导出', @menuId, 5, '#', '', 1, 0, 'F', '0', '0', 'demo:sse:export', '#', 'admin', sysdate(), '', null, ''); 