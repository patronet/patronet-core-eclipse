<?php

namespace ${namespace};

use PatroNet\Core\Entity\ActiveRecordEntity;
use PatroNet\Core\Database\ActiveRecord;

class ${class} extends ActiveRecordEntity {
    
    private static $$oRepository = null;
    
    public function __construct(ActiveRecord $$oActiveRecord)
    {
        parent::__construct($$oActiveRecord);
    }
    
    /**
     * @return ${class}\_Repository
     */
    public static function getRepository()
    {
        if (is_null(self::$$oRepository)) {
            self::$$oRepository = new ${class}\_Repository();
        }
        return self::$$oRepository;
    }
    
}

namespace ${namespace}\${class};

use PatroNet\Core\Database\ActiveRecord;
use PatroNet\Core\Entity\TableRepository;
use PatroNet\Cms\Application\Application;
use ${namespace}\${class};

/**
 * @method ${class} create()
 * @method ${class} get(mixed $$id)
 * @method ${class}[]|\PatroNet\Core\Database\ResultSet getAll(int[] $$idList = null, string[string] $$order = null, mixed $$limit = null)
 * @method ${class}[]|\PatroNet\Core\Database\ResultSet getAllByFilter(mixed $$filter = null, string[string] $$order = null, mixed $$limit = null)
 */
class _Repository extends TableRepository
{
    
    const TABLE = "${tableName}";
    
    const TABLE_KEY = "${tableIdCell}";
    
    const TABLE_ALIAS = "${tableAlias}";
    
    public function __construct()
    {
        parent::__construct($$oTable = Application::conn()->getTable(self::TABLE, self::TABLE_KEY, self::TABLE_ALIAS));
    }
    
    protected function wrapActiveRecordToEntity(ActiveRecord $$oActiveRecord)
    {
        return new ${class}($$oActiveRecord);
    }
    
}